package org.gradle.samples.internal;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.samples.Sample;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SamplesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("lifecycle-base");
        NamedDomainObjectContainer<Sample> samples = project.container(Sample.class, name -> {
            assert !name.isEmpty() : "sample name cannot be empty";
            return project.getObjects().newInstance(DefaultSample.class, name);
        });
        project.getExtensions().add(NamedDomainObjectContainer.class, "samples", samples);

        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getConfigurations().maybeCreate("asciidoctor");
        project.getDependencies().add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0");

        samples.configureEach(sample -> {
            createWrapperTask(project.getTasks(), sample, project.getLayout());

            createSyncGroovyDslTask(project.getTasks(), sample, project.getLayout());
            TaskProvider<? extends Task> groovyDslZipTask = createGroovyDslZipTask(project.getTasks(), sample, project.getLayout());

            createSyncKotlinDslTask(project.getTasks(), sample, project.getLayout());
            TaskProvider<? extends Task> kotlinDslZipTask = createKotlinDslZipTask(project.getTasks(), sample, project.getLayout());

            TaskProvider<? extends Task> asciidocTask = createAsciidocTask(project.getTasks(), sample, project.getLayout());

            TaskProvider<? extends Task> assembleTask = createSampleAssembleTask(project.getTasks(), sample, Arrays.asList(groovyDslZipTask, kotlinDslZipTask, asciidocTask));

            project.getTasks().named("assemble").configure(it -> it.dependsOn(assembleTask));
        });

        TaskProvider<GenerateSampleIndexAsciidoc> indexGeneratorTask = createSampleIndexGeneratorTask(project.getTasks(), samples, project.getLayout(), project.getProviders());
        TaskProvider<? extends Task> asciidocTask = createIndexAsciidocTask(project.getTasks(), indexGeneratorTask, project.getLayout());
        project.getTasks().named("assemble").configure(it -> it.dependsOn(asciidocTask));
    }

    private static TaskProvider<Sync> createSyncGroovyDslTask(TaskContainer tasks, Sample sample, ProjectLayout projectLayout) {
        return tasks.register(syncGroovyDslTaskName(sample), Sync.class, task -> {
            task.dependsOn(generateWrapperTaskName(sample));
            task.setDestinationDir(projectLayout.getBuildDirectory().dir("sample-zips/" + sample.getName() + "/groovy-dsl").get().getAsFile());
            task.from(projectLayout.getBuildDirectory().dir("sample-wrappers/" + sample.getName()));
            task.from(sample.getSampleDir().file("README.adoc"));
            // TODO(daniel): We should probably use `groovy-dsl`, however, we are following the gradle/gradle convention for now
            task.from(sample.getSampleDir().dir("groovy"));
        });
    }

    private static TaskProvider<Sync> createSyncKotlinDslTask(TaskContainer tasks, Sample sample, ProjectLayout projectLayout) {
        return tasks.register(syncKotlinDslTaskName(sample), Sync.class, task -> {
            task.dependsOn(generateWrapperTaskName(sample));
            task.setDestinationDir(projectLayout.getBuildDirectory().dir("sample-zips/" + sample.getName() + "/kotlin-dsl").get().getAsFile());
            task.from(projectLayout.getBuildDirectory().dir("sample-wrappers/" + sample.getName()));
            task.from(sample.getSampleDir().file("README.adoc"));
            // TODO(daniel): We should probably use `kotlin-dsl`, however, we are following the gradle/gradle convention for now
            task.from(sample.getSampleDir().dir("kotlin"));
        });
    }

    private static TaskProvider<Wrapper> createWrapperTask(TaskContainer tasks, Sample sample, ProjectLayout projectLayout) {
        return tasks.register(generateWrapperTaskName(sample), Wrapper.class, task -> {
            task.setJarFile(projectLayout.getBuildDirectory().file("sample-wrappers/" + sample.getName() + "/gradle/wrapper/gradle-wrapper.jar").get().getAsFile());
            task.setScriptFile(projectLayout.getBuildDirectory().file("sample-wrappers/" + sample.getName() + "/gradlew").get().getAsFile());
        });
    }

    private static TaskProvider<SampleZipTask> createGroovyDslZipTask(TaskContainer tasks, Sample sample, ProjectLayout projectLayout) {
        return tasks.register(compressSampleGroovyDslTaskName(sample), SampleZipTask.class, task -> {
            task.dependsOn(syncGroovyDslTaskName(sample));

            task.getSampleDirectory().set(projectLayout.getBuildDirectory().dir("sample-zips/" + sample.getName() + "/groovy-dsl"));
            task.getSampleZipFile().set(projectLayout.getBuildDirectory().file("gradle-samples/" + sample.getName() + "/" + sample.getName() + "-groovy-dsl.zip"));
        });
    }

    private static TaskProvider<SampleZipTask> createKotlinDslZipTask(TaskContainer tasks, Sample sample, ProjectLayout projectLayout) {
        return tasks.register(compressSampleKotlinDslTaskName(sample), SampleZipTask.class, task -> {
            task.dependsOn(syncKotlinDslTaskName(sample));

            task.getSampleDirectory().set(projectLayout.getBuildDirectory().dir("sample-zips/" + sample.getName() + "/kotlin-dsl"));
            task.getSampleZipFile().set(projectLayout.getBuildDirectory().file("gradle-samples/" + sample.getName() + "/" + sample.getName() + "-kotlin-dsl.zip"));
        });
    }

    private static TaskProvider<Task> createSampleAssembleTask(TaskContainer tasks, Sample sample, Iterable<? extends TaskProvider> taskDependencies) {
        return tasks.register("assemble" + capitalize(sample.getName()) + "Sample", task -> {
            task.dependsOn(taskDependencies);
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample");
        });
    }

    private static TaskProvider<AsciidoctorTask> createAsciidocTask(TaskContainer tasks, Sample sample, ProjectLayout projectLayout) {
        return tasks.register("asciidoctor" + capitalize(sample.getName()) + "Sample", AsciidoctorTask.class, task -> {
            task.dependsOn(compressSampleKotlinDslTaskName(sample), compressSampleGroovyDslTaskName(sample));
            task.sourceDir(sample.getSampleDir());
            task.sources(new Closure<Void>(task, task) {
                @Override
                public Void call() {
                    PatternSet pattern = (PatternSet) getDelegate();
                    pattern.include("README.adoc");
                    return null;
                }
            });
            task.outputDir(task.getTemporaryDir());
            task.setSeparateOutputDirs(false);
            task.doLast(it -> {
                task.getProject().copy(spec -> {
                    spec.from(task.getTemporaryDir());
                    spec.rename("README.html", "index.html");
                    spec.into(projectLayout.getBuildDirectory().dir("gradle-samples/" + sample.getName()));
                });
            });
            // TODO set inputs

            Map<String, Object> a = new HashMap<>();
            a.put("source-highlighter", "prettify");
            a.put("imagesdir", "images");
            a.put("stylesheet", null);
            a.put("linkcss", true);
            a.put("docinfodir", ".");
            a.put("docinfo1", "");
            a.put("nofooter", true);
            a.put("icons", "font");
            a.put("sectanchors", true);
            a.put("sectlinks", true);
            a.put("linkattrs", true);
            a.put("encoding", "utf-8");
            a.put("idprefix", "");
            a.put("toc", "auto");
            a.put("toclevels", 1);
            a.put("toc-title", "Contents");
            a.put("guides", "https://guides.gradle.org");
            task.setAttributes(a);

//            def asciidocIndexFile = project.layout.file(project.tasks.named("asciidoctor").map { new File(it.outputDir, "html5/index.html") })
//
//        project.tasks.register("viewGuide", ViewGuide) {
//            group = "Documentation"
//            description = "Generates the guide and open in the browser"
//            indexFile = asciidocIndexFile
//        }
        });
    }

    private static String compressSampleKotlinDslTaskName(Sample sample) {
        return "compress" + capitalize(sample.getName()) + "KotlinDslSample";
    }

    private static String compressSampleGroovyDslTaskName(Sample sample) {
        return "compress" + capitalize(sample.getName()) + "GroovyDslSample";
    }

    private static String syncGroovyDslTaskName(Sample sample) {
        return "sync" + capitalize(sample.getName()) + "GroovyDslSample";
    }

    private static String syncKotlinDslTaskName(Sample sample) {
        return "sync" + capitalize(sample.getName()) + "KotlinDslSample";
    }

    private static String generateWrapperTaskName(Sample sample) {
        return "generateWrapperFor" + capitalize(sample.getName()) + "Sample";
    }

    private static TaskProvider<GenerateSampleIndexAsciidoc> createSampleIndexGeneratorTask(TaskContainer tasks, Iterable<Sample> samples, ProjectLayout projectLayout, ProviderFactory providerFactory) {
        return tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.getSamplePaths().set(providerFactory.provider(() -> StreamSupport.stream(samples.spliterator(), false).map(Sample::getName).collect(Collectors.toList())));
            task.getOutputFile().set(new File(task.getTemporaryDir(), "index.adoc"));
        });
    }

    private static TaskProvider<AsciidoctorTask> createIndexAsciidocTask(TaskContainer tasks, TaskProvider<GenerateSampleIndexAsciidoc> generatorTask, ProjectLayout projectLayout) {
        return tasks.register("asciidocSampleIndex", AsciidoctorTask.class, task -> {
            task.dependsOn(generatorTask);
            task.sourceDir(generatorTask.get().getOutputFile().get().getAsFile().getParentFile());
            task.outputDir(projectLayout.getBuildDirectory().dir("gradle-samples"));
            task.setSeparateOutputDirs(false);
            task.setBackends("html5");
            // TODO set inputs

            Map<String, Object> a = new HashMap<>();
            a.put("source-highlighter", "prettify");
            a.put("imagesdir", "images");
            a.put("stylesheet", null);
            a.put("linkcss", true);
            a.put("docinfodir", ".");
            a.put("docinfo1", "");
            a.put("nofooter", true);
            a.put("icons", "font");
            a.put("sectanchors", true);
            a.put("sectlinks", true);
            a.put("linkattrs", true);
            a.put("encoding", "utf-8");
            a.put("idprefix", "");
            a.put("toc", "auto");
            a.put("toclevels", 1);
            a.put("toc-title", "Contents");
            a.put("guides", "https://guides.gradle.org");
            task.setAttributes(a);
        });
    }

    private static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }
}
