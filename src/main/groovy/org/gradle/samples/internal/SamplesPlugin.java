package org.gradle.samples.internal;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.samples.Sample;
import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SamplesPlugin implements Plugin<Project> {
    private enum Dsl {
        GROOVY_DSL("groovy"), KOTLIN_DSL("kotlin");

        private final String languageName;

        Dsl(String languageName) {
            this.languageName = languageName;
        }

        String getSampleDirectoryName() {
            return languageName;
        }

        String getClassifierName() {
            return languageName + "-dsl";
        }

        public String getTaskName() {
            return capitalize(languageName) + "Dsl";
        }
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("lifecycle-base");
        NamedDomainObjectContainer<Sample> samples = project.container(Sample.class, name -> {
            assert !name.isEmpty() : "sample name cannot be empty";
            DefaultSample result = project.getObjects().newInstance(DefaultSample.class, name);
            result.getGradleVersion().convention(project.getGradle().getGradleVersion());
            return result;
        });
        project.getExtensions().add(NamedDomainObjectContainer.class, "samples", samples);

        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getConfigurations().maybeCreate("asciidoctor");
        project.getDependencies().add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0");

        Provider<Directory> sampleIntermediateDirectory = project.getLayout().getBuildDirectory().dir("sample-intermediate");

        samples.configureEach(s -> {
            DefaultSample sample = (DefaultSample) s;
            Provider<String> zipBaseFileName = project.provider(() -> sample.getName() + (project.getVersion().equals(Project.DEFAULT_VERSION) ? "" : "-" + project.getVersion().toString()));

            createWrapperTask(project.getTasks(), sample, sampleIntermediateDirectory);
            TaskProvider<? extends Task> asciidoctorTask = createAsciidoctorTask(project.getTasks(), sample, zipBaseFileName, sampleIntermediateDirectory);
            TaskProvider<Sync> assembleTask = createSampleAssembleTask(project.getTasks(), sample, project.getLayout().getBuildDirectory(), sampleIntermediateDirectory, Arrays.asList(asciidoctorTask));

            project.getTasks().named("assemble").configure(it -> it.dependsOn(assembleTask));

            sample.getDslSampleArchives().configureEach(sampleDsl -> {
                createSyncDslTask(project.getTasks(), sample, sampleIntermediateDirectory, sampleDsl.getName().contains("groovy") ? Dsl.GROOVY_DSL : Dsl.KOTLIN_DSL);
                TaskProvider<SampleZipTask> zipTask = createDslZipTask(project.getTasks(), sample, zipBaseFileName, sampleIntermediateDirectory, sampleDsl.getName().contains("groovy") ? Dsl.GROOVY_DSL : Dsl.KOTLIN_DSL);

                assembleTask.configure(task -> task.from(zipTask.flatMap(SampleZipTask::getSampleZipFile)));
            });
        });

        TaskProvider<GenerateSampleIndexAsciidoc> indexGeneratorTask = createSampleIndexGeneratorTask(project.getTasks(), samples, project.getLayout(), project.getProviders());
        TaskProvider<? extends Task> asciidocTask = createIndexAsciidocTask(project.getTasks(), indexGeneratorTask, project.getLayout());
        project.getTasks().named("assemble").configure(it -> it.dependsOn(asciidocTask));
    }

    private static TaskProvider<Sync> createSyncDslTask(TaskContainer tasks, Sample sample, Provider<Directory> sampleIntermediateDirectory, Dsl dsl) {
        return tasks.register(syncDslTaskName(sample, dsl), Sync.class, task -> {
            task.dependsOn(generateWrapperTaskName(sample));
            task.into(sampleIntermediateDirectory.map(dir -> dir.dir(sample.getName() + "-" + dsl.getClassifierName())));
            task.from(sampleIntermediateDirectory.map(dir -> dir.dir(sample.getName() + "-wrapper")));
            task.from(sample.getSampleDir().file("README.adoc"));
            task.from(sample.getArchiveContent());

            // TODO(daniel): We should probably use `(groovy|kotlin)-dsl`, however, we are following the gradle/gradle convention for now
            task.from(sample.getSampleDir().dir(dsl.getSampleDirectoryName()));
            task.onlyIf(it -> !sample.getSampleDir().dir(dsl.getSampleDirectoryName()).get().getAsFileTree().isEmpty());
        });
    }

    private static TaskProvider<Wrapper> createWrapperTask(TaskContainer tasks, Sample sample, Provider<Directory> sampleIntermediateDirectory) {
        return tasks.register(generateWrapperTaskName(sample), Wrapper.class, task -> {
            task.setJarFile(sampleIntermediateDirectory.get().dir(sample.getName() + "-wrapper/gradle/wrapper/gradle-wrapper.jar").getAsFile());
            task.setScriptFile(sampleIntermediateDirectory.get().dir(sample.getName() + "-wrapper/gradlew").getAsFile());
            task.setGradleVersion(sample.getGradleVersion().get());
            task.onlyIf(it -> !sample.getSampleDir().getAsFileTree().isEmpty());
        });
    }

    private static TaskProvider<SampleZipTask> createDslZipTask(TaskContainer tasks, Sample sample, Provider<String> zipBaseFileName, Provider<Directory> sampleIntermediateDirectory, Dsl dsl) {
        return tasks.register(compressSampleDslTaskName(sample, dsl), SampleZipTask.class, task -> {
            task.dependsOn(syncDslTaskName(sample, dsl));

            task.getSampleDirectory().set(sampleIntermediateDirectory.map(dir -> dir.dir(sample.getName() + "-" + dsl.getClassifierName())));
            task.getSampleZipFile().set(sampleIntermediateDirectory.map(dir -> dir.file(sample.getName() + "-" + dsl.getClassifierName() + "-zip/" + zipBaseFileName.get() + "-" + dsl.getClassifierName() + ".zip")));
        });
    }

    private static TaskProvider<Sync> createSampleAssembleTask(TaskContainer tasks, Sample sample, Provider<Directory> buildDirectory, Provider<Directory> sampleIntermediateDirectory, Iterable<? extends TaskProvider> taskDependencies) {
        return tasks.register("assemble" + GUtil.toCamelCase(sample.getName()) + "Sample", Sync.class, task -> {
            task.dependsOn(taskDependencies);
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample");

            task.from(sampleIntermediateDirectory.map(dir -> dir.dir(sample.getName() + "-content")), spec -> {
                spec.rename("README.html", "index.html");
            });
            task.into(buildDirectory.map(dir -> dir.dir("gradle-samples/" + sample.getName())));
        });
    }

    private static TaskProvider<AsciidoctorTask> createAsciidoctorTask(TaskContainer tasks, Sample sample, Provider<String> zipBaseFileName, Provider<Directory> sampleIntermediateDirectory) {
        return tasks.register("asciidoctor" + GUtil.toCamelCase(sample.getName()) + "Sample", AsciidoctorTask.class, task -> {
            task.sourceDir(sample.getSampleDir());
            task.outputDir(sampleIntermediateDirectory.map(dir -> dir.dir(sample.getName() + "-content")));
            task.setSeparateOutputDirs(false);

            Map<String, Object> a = getAsciidoctorAttributes();
            a.put("zip-base-file-name", zipBaseFileName.get());
            task.attributes(a);
        });
    }

    private static String compressSampleDslTaskName(Sample sample, Dsl dsl) {
        return "compress" + GUtil.toCamelCase(sample.getName()) + dsl.getTaskName() + "Sample";
    }

    private static String syncDslTaskName(Sample sample, Dsl dsl) {
        return "sync" + GUtil.toCamelCase(sample.getName()) + dsl.getTaskName() + "Sample";
    }

    private static String generateWrapperTaskName(Sample sample) {
        return "generateWrapperFor" + GUtil.toCamelCase(sample.getName()) + "Sample";
    }

    private static TaskProvider<GenerateSampleIndexAsciidoc> createSampleIndexGeneratorTask(TaskContainer tasks, Iterable<Sample> samples, ProjectLayout projectLayout, ProviderFactory providerFactory) {
        return tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.getSamplePaths().set(providerFactory.provider(() -> StreamSupport.stream(samples.spliterator(), false).filter(it -> !it.getSampleDir().getAsFileTree().isEmpty()).map(Sample::getName).collect(Collectors.toList())));
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

    private static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }
}
