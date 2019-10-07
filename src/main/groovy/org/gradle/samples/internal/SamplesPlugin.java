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
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.samples.Sample;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.gradle.samples.internal.StringUtils.capitalize;

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
            result.getArchiveBaseName().set(project.provider(() -> name + (project.getVersion().equals(Project.DEFAULT_VERSION) ? "" : "-" + project.getVersion().toString())));
            result.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("gradle-samples/" + name));
            result.getSampleDirectory().convention(project.getLayout().getProjectDirectory().dir("src/samples/" + name));
            return result;
        });
        project.getExtensions().add(NamedDomainObjectContainer.class, "samples", samples);

        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getConfigurations().maybeCreate("asciidoctor");
        project.getDependencies().add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0");

        samples.configureEach(s -> {
            DefaultSample sample = (DefaultSample) s;

            // TODO: avoid creating the task if no DSL sample archive
            createWrapperTask(project.getTasks(), sample, getObjectFactory());
            createAsciidoctorTask(project.getTasks(), sample, getObjectFactory());
            TaskProvider<Sync> assembleTask = createSampleAssembleTask(project.getTasks(), sample);

            project.getTasks().named("assemble").configure(it -> it.dependsOn(assembleTask));

            sample.getDslSampleArchives().configureEach(dslSample -> {
                TaskProvider<Sync> syncTask = createSyncDslTask(project.getTasks(), dslSample);
                TaskProvider<SampleZipTask> zipTask = createDslZipTask(project.getTasks(), dslSample);

                checkForValidSampleArchive(syncTask, sample, dslSample);

                sample.getSource().from(zipTask.flatMap(SampleZipTask::getSampleZipFile));
            });
        });

        TaskProvider<GenerateSampleIndexAsciidoc> indexGeneratorTask = createSampleIndexGeneratorTask(project.getTasks(), samples, project.getLayout(), project.getProviders());
        TaskProvider<? extends Task> asciidocTask = createIndexAsciidocTask(project.getTasks(), indexGeneratorTask, project.getLayout());
        project.getTasks().named("assemble").configure(it -> it.dependsOn(asciidocTask));

        project.afterEvaluate(evaluatedProject -> {
            samples.stream().map(it -> (DefaultSample)it).forEach(this::configureDefaultDomainSpecificSampleIfNeeded);
        });
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

    private static TaskProvider<Sync> createSyncDslTask(TaskContainer tasks, DslSampleArchive dslSample) {
        return tasks.register(dslSample.getSyncTaskName(), Sync.class, task -> {
            task.from(dslSample.getArchiveContent());
            task.into(dslSample.getAssembleDirectory());
        });
    }

    private static void checkForValidSampleArchive(TaskProvider<Sync> syncTask, DefaultSample sample, DslSampleArchive dslSample) {
        syncTask.configure(task -> {
            task.doLast(new Action<Task>() {
                // Lambda isn't well supported yet
                @Override
                public void execute(Task it) {
                    if (!dslSample.getAssembleDirectory().file(dslSample.getSettingsFileName()).get().getAsFile().exists()) {
                        throw new GradleException("Sample '" + sample.getName() + "' for " + capitalize(dslSample.getLanguageName()) + " DSL is invalid due to missing '" + dslSample.getSettingsFileName() + "' file.");
                    }
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

        sample.getDslSampleArchives().all(it -> it.getArchiveContent().from(objectFactory.fileCollection().from(wrapperDirectory).builtBy(wrapperTask)));

        return wrapperTask;
    }

    private static TaskProvider<SampleZipTask> createDslZipTask(TaskContainer tasks, DslSampleArchive dslSample) {
        return tasks.register(dslSample.getCompressTaskName(), SampleZipTask.class, task -> {
            task.dependsOn(dslSample.getSyncTaskName()); // TODO: Eliminate this

            task.getSampleDirectory().set(dslSample.getAssembleDirectory());
            task.getSampleZipFile().set(dslSample.getArchiveFile());
        });
    }

    private static TaskProvider<Sync> createSampleAssembleTask(TaskContainer tasks, DefaultSample sample) {
        return tasks.register("assemble" + capitalize(sample.getName()) + "Sample", Sync.class, task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample");

            task.from(sample.getSource());
            task.into(sample.getOutputDirectory());
        });
    }

    private static TaskProvider<AsciidoctorTask> createAsciidoctorTask(TaskContainer tasks, DefaultSample sample, ObjectFactory objectFactory) {
        Provider<Directory> contentDirectory = sample.getIntermediateDirectory().dir("content");
        TaskProvider<AsciidoctorTask> asciidoctorTask = tasks.register("asciidoctor" + capitalize(sample.getName()) + "Sample", AsciidoctorTask.class, task -> {
            task.sourceDir(sample.getSampleDirectory());
            task.outputDir(task.getProject().getLayout().getBuildDirectory().dir("tmp/" + task.getName()));
            task.setSeparateOutputDirs(false);

            task.doLast(it -> {
                task.getProject().sync(spec -> {
                    spec.from(task.getTemporaryDir());
                    spec.into(contentDirectory);
                    spec.rename("README.html", "index.html");
                });
            });
            // TODO: Filter to only README.adoc
            // TODO: Fail if no README.adoc file

            Map<String, Object> a = getAsciidoctorAttributes();
            a.put("zip-base-file-name", sample.getArchiveBaseName().get());
            task.attributes(a);
        });

        sample.getSource().from(objectFactory.fileCollection().from(contentDirectory).builtBy(asciidoctorTask));

        return asciidoctorTask;
    }

    private static TaskProvider<GenerateSampleIndexAsciidoc> createSampleIndexGeneratorTask(TaskContainer tasks, Iterable<Sample> samples, ProjectLayout projectLayout, ProviderFactory providerFactory) {
        return tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.getSamplePaths().set(providerFactory.provider(() -> StreamSupport.stream(samples.spliterator(), false).filter(it -> !it.getSampleDirectory().getAsFileTree().isEmpty()).map(Sample::getName).collect(Collectors.toList())));
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
