package org.gradle.samples.internal;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.samples.Sample;
import org.gradle.samples.internal.tasks.GenerateSampleIndexAsciidoc;
import org.gradle.samples.internal.tasks.InstallSampleTask;
import org.gradle.samples.internal.tasks.InstallSampleZipContentTask;
import org.gradle.samples.internal.tasks.SampleZipTask;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.gradle.samples.internal.StringUtils.capitalize;
import static org.gradle.samples.internal.StringUtils.toTitleCase;

@SuppressWarnings("Convert2Lambda") // Additional task actions are not supported to be lambdas
public class SamplesPlugin implements Plugin<Project> {
    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("lifecycle-base");
        NamedDomainObjectContainer<Sample> samples = project.container(Sample.class, name -> {
            assert !name.isEmpty() : "sample name cannot be empty";

            DefaultSample result = project.getObjects().newInstance(DefaultSample.class, name);
            result.getGradleVersion().convention(project.getGradle().getGradleVersion());
            result.getIntermediateDirectory().set(project.getLayout().getBuildDirectory().dir("sample-intermediate/" + name));
            result.getArchiveBaseName().set(project.provider(() -> capitalize(name) + (project.getVersion().equals(Project.DEFAULT_VERSION) ? "" : "-" + project.getVersion().toString())));
            result.getInstallDirectory().set(project.getLayout().getBuildDirectory().dir("gradle-samples/" + name));
            result.getSampleDirectory().convention(project.getLayout().getProjectDirectory().dir("src/samples/" + name));
            result.getDisplayName().convention(toTitleCase(name));
            result.setAsciidoctorTask(createAsciidoctorTask(project.getTasks(), result, getObjectFactory()));

            return result;
        });
        project.getExtensions().add(NamedDomainObjectContainer.class, "samples", samples);

        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getConfigurations().maybeCreate("asciidoctor");
        project.getDependencies().add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0");

        samples.configureEach(s -> {
            DefaultSample sample = (DefaultSample) s;

            sample.getArchiveContent().from(findLicenseFile(project));

            // TODO: Generate basic README (see kotlin-dsl)
            // TODO: Process the README file to add links to the archives
            createWrapperTask(project.getTasks(), sample, getObjectFactory());
            TaskProvider<InstallSampleTask> installSampleTask = createSampleInstallTask(project.getTasks(), sample);
            TaskProvider<Task> assembleTask = createSampleAssembleTask(project.getTasks(), sample);
            assembleTask.configure(it -> it.dependsOn(installSampleTask));

            project.getTasks().named("assemble").configure(it -> it.dependsOn(assembleTask));

            sample.getDslSampleArchives().configureEach(dslSample -> {
                TaskProvider<InstallSampleZipContentTask> installTask = createSampleDslInstallTask(project.getTasks(), dslSample);
                TaskProvider<SampleZipTask> zipTask = createDslZipTask(project.getTasks(), dslSample);

                checkForValidSampleArchive(installTask, sample, dslSample);

                sample.getSource().from(zipTask.flatMap(SampleZipTask::getSampleZipFile));
            });
        });

        List<Sample> orderedSampleList = new ArrayList<>();
        samples.configureEach(orderedSampleList::add);
        TaskProvider<GenerateSampleIndexAsciidoc> indexGeneratorTask = createSampleIndexGeneratorTask(project.getTasks(), orderedSampleList, project.getLayout(), project.getProviders());

        TaskProvider<? extends Task> asciidocTask = createIndexAsciidocTask(project.getTasks(), indexGeneratorTask, project.getLayout());
        project.getTasks().named("assemble").configure(it -> it.dependsOn(asciidocTask));

        project.afterEvaluate(evaluatedProject -> {
            samples.stream().map(it -> (DefaultSample) it).forEach(this::configureDefaultDomainSpecificSampleIfNeeded);
        });

        project.getPluginManager().apply(TestingSamplesWithExemplarPlugin.class);
    }

    private static Callable<List<File>> findLicenseFile(Project project) {
        return () -> {
            Project rootProject = project.getRootProject();
            return Stream.of("LICENSE", "LICENSE.txt", "LICENSE.md", "LICENSE.adoc")
                    .map(rootProject::file)
                    .filter(File::exists)
                    .findAny()
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        };
    }

    private void configureDefaultDomainSpecificSampleIfNeeded(DefaultSample sample) {
        if (sample.getDslSampleArchives().isEmpty()) {
            if (KotlinDslSampleArchive.hasSource(sample.getSampleDirectory().get())) {
                sample.getDslSampleArchives().add(getObjectFactory().newInstance(KotlinDslSampleArchive.class, sample.getName()).configureFrom(sample));
            }
            if (GroovyDslSampleArchive.hasSource(sample.getSampleDirectory().get())) {
                sample.getDslSampleArchives().add(getObjectFactory().newInstance(GroovyDslSampleArchive.class, sample.getName()).configureFrom(sample));
            }
        }
        // TODO: Print warning when assembling sample if no zip
    }

    private static TaskProvider<InstallSampleZipContentTask> createSampleDslInstallTask(TaskContainer tasks, DslSampleArchive dslSample) {
        return tasks.register(dslSample.getInstallTaskName(), InstallSampleZipContentTask.class, task -> {
            task.getSource().from(dslSample.getArchiveContent());
            task.getInstallDirectory().set(dslSample.getInstallDirectory());
        });
    }

    private static void checkForValidSampleArchive(TaskProvider<InstallSampleZipContentTask> installTask, DefaultSample sample, DslSampleArchive dslSample) {
        installTask.configure(task -> {
            task.doFirst(new Action<Task>() {
                // Lambda isn't well supported yet
                @Override
                public void execute(Task it) {
                    if (!hasSettingsFile()) {
                        throw new GradleException("Sample '" + sample.getName() + "' for " + capitalize(dslSample.getLanguageName()) + " DSL is invalid due to missing '" + dslSample.getSettingsFileName() + "' file.");
                    }

                    if (!hasReadMeFile()) {
                        throw new GradleException("Sample '" + sample.getName() + "' is invalid due to missing 'README.adoc' file.");
                    }
                }

                private boolean hasSettingsFile() {
                    return task.getSource().getAsFileTree().getFiles().stream().anyMatch(it -> it.getName().equals(dslSample.getSettingsFileName()));
                }

                private boolean hasReadMeFile() {
                    return task.getSource().getAsFileTree().getFiles().stream().anyMatch(it -> it.getName().equals("README.adoc"));
                }
            });
        });
    }

    private static TaskProvider<Wrapper> createWrapperTask(TaskContainer tasks, DefaultSample sample, ObjectFactory objectFactory) {
        Provider<Directory> wrapperDirectory = sample.getIntermediateDirectory().dir("wrapper");
        TaskProvider<Wrapper> wrapperTask = tasks.register(sample.getWrapperTaskName(), Wrapper.class, task -> {
            task.setJarFile(wrapperDirectory.get().dir("gradle/wrapper/gradle-wrapper.jar").getAsFile());
            task.setScriptFile(wrapperDirectory.get().file("gradlew").getAsFile());
            task.setGradleVersion(sample.getGradleVersion().get());
            task.onlyIf(it -> !sample.getSampleDirectory().getAsFileTree().isEmpty());
        });

        sample.getDslSampleArchives().all(it -> {
            it.getArchiveContent().from(objectFactory.fileCollection().from(wrapperDirectory).builtBy(wrapperTask));
        });

        return wrapperTask;
    }

    private static TaskProvider<SampleZipTask> createDslZipTask(TaskContainer tasks, DslSampleArchive dslSample) {
        return tasks.register(dslSample.getCompressTaskName(), SampleZipTask.class, task -> {
            task.dependsOn(dslSample.getInstallTaskName()); // TODO: Eliminate this

            task.getSampleDirectory().set(dslSample.getInstallDirectory());
            task.getSampleZipFile().set(dslSample.getArchiveFile());
        });
    }

    private static TaskProvider<InstallSampleTask> createSampleInstallTask(TaskContainer tasks, DefaultSample sample) {
        return tasks.register("install" + capitalize(sample.getName()) + "Sample", InstallSampleTask.class, task -> {
            task.getSource().from(sample.getSource());
            task.getInstallDirectory().set(sample.getInstallDirectory());
        });
    }

    private static TaskProvider<Task> createSampleAssembleTask(TaskContainer tasks, DefaultSample sample) {
        return tasks.register("assemble" + capitalize(sample.getName()) + "Sample", Task.class, task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample");
        });
    }

    // Essentially `static`. See StringProvider.
    private TaskProvider<AsciidoctorTask> createAsciidoctorTask(TaskContainer tasks, DefaultSample sample, ObjectFactory objectFactory) {
        Provider<Directory> contentDirectory = sample.getIntermediateDirectory().dir("content");
        TaskProvider<AsciidoctorTask> asciidoctorTask = tasks.register("asciidoctor" + capitalize(sample.getName()) + "Sample", AsciidoctorTask.class, task -> {
            task.sourceDir(sample.getSampleDirectory());
            task.outputDir(task.getProject().getLayout().getBuildDirectory().dir("tmp/" + task.getName() + "/out"));
            task.setSeparateOutputDirs(false);

            Provider<String> zipBaseFileName = sample.getArchiveBaseName();
            task.getInputs().property("zipBaseFileName", zipBaseFileName);
            task.doFirst(new Action<Task>() {
                @Override
                public void execute(Task it) {
                    task.getAttributes().put("zip-base-file-name", zipBaseFileName.get());
                }
            });

            Provider<Directory> samplesDir = sample.getSampleDirectory();
            task.getInputs().dir(samplesDir).withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE);
            task.doFirst(new Action<Task>() {
                @Override
                public void execute(Task it) {
                    task.getAttributes().put("samples-dir", samplesDir.get().getAsFile().getAbsolutePath());
                }
            });

            Provider<String> sampleDisplayName = sample.getDisplayName();
            task.getInputs().property("sampleDisplayName", sampleDisplayName);
            task.doFirst(new Action<Task>() {
                @Override
                public void execute(Task it) {
                    task.getAttributes().put("sample-displayName", sampleDisplayName.get());
                }
            });

            Provider<String> sampleDescription = sample.getDescription();
            task.getInputs().property("sampleDescription", sampleDescription).optional(true);
            task.doFirst(new Action<Task>() {
                @Override
                public void execute(Task it) {
                    if (sampleDescription.isPresent()) {
                        task.getAttributes().put("sample-description", sampleDescription.get());
                    }
                }
            });

            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task it) {
                    task.getProject().sync(spec -> {
                        spec.from(new File(task.getTemporaryDir(), "out"));
                        spec.into(contentDirectory);
                        spec.rename("README.html", "index.html");
                    });
                }
            });
            // TODO: Filter to only README.adoc
            // TODO: Fail if no README.adoc file

            task.attributes(getAsciidoctorAttributes());
        });

        sample.getSource().from(objectFactory.fileCollection().from(contentDirectory).builtBy(asciidoctorTask));

        return asciidoctorTask;
    }

    private static TaskProvider<GenerateSampleIndexAsciidoc> createSampleIndexGeneratorTask(TaskContainer tasks, Iterable<Sample> samples, ProjectLayout projectLayout, ProviderFactory providerFactory) {
        return tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.getSampleInformation().set(providerFactory.provider(() -> StreamSupport.stream(samples.spliterator(), false)
                    .filter(it -> !it.getSampleDirectory().getAsFileTree().isEmpty())
                    .map(it -> new GenerateSampleIndexAsciidoc.SampleInformation(it.getName(), it.getDisplayName().get(), it.getDescription().getOrNull()))
                    .collect(Collectors.toList())));
            task.getOutputFile().set(projectLayout.getBuildDirectory().file("tmp/" + task.getName() + "/index.adoc"));
        });
    }

    private static TaskProvider<AsciidoctorTask> createIndexAsciidocTask(TaskContainer tasks, TaskProvider<GenerateSampleIndexAsciidoc> generatorTask, ProjectLayout projectLayout) {
        return tasks.register("asciidocSampleIndex", AsciidoctorTask.class, task -> {
            task.dependsOn(generatorTask);
            task.sourceDir(generatorTask.get().getOutputFile().get().getAsFile().getParentFile());
            task.outputDir(projectLayout.getBuildDirectory().dir("gradle-samples"));
            task.setSeparateOutputDirs(false);
            task.setBackends("html5");
            task.attributes(getAsciidoctorAttributes());
        });
    }

    private static Map<String, Object> getAsciidoctorAttributes() {
        Map<String, Object> a = new HashMap<>();
        a.put("source-highlighter", "prettify");
        a.put("imagesdir", "images");
        a.put("docinfodir", ".");
        a.put("docinfo1", "");
        a.put("icons", "font");
        a.put("encoding", "utf-8");
        a.put("idprefix", "");
        a.put("toc", "auto");
        a.put("toclevels", 1);
        a.put("toc-title", "Contents");
        return a;
    }
}
