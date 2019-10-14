package org.gradle.samples.internal;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.samples.ExemplarExtension;
import org.gradle.samples.Sample;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import static org.gradle.samples.internal.StringUtils.capitalize;

public class TestingSamplesWithExemplarPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-base");
        NamedDomainObjectContainer<Sample> samples = (NamedDomainObjectContainer<Sample>) project.getExtensions().getByName("samples");

        Provider<Directory> samplesExemplarDirectory = project.getLayout().getBuildDirectory().dir("samples-exemplar");

        SourceSet sourceSet = createSourceSet(project.getExtensions().getByType(SourceSetContainer.class));
        TaskProvider<Task> generatorTask = createExemplarGeneratorTask(project.getTasks(), project.getLayout(), sourceSet);
        sourceSet.getJava().srcDir(generatorTask);

        configureExemplarDependencies(project.getDependencies(), sourceSet);

        TaskProvider<Sync> installTask = createExemplarInstallTask(project.getTasks(), sourceSet, samplesExemplarDirectory);
        TaskProvider<Test> testTask = createExemplarTestTask(project.getTasks(), sourceSet, project.getLayout(), samplesExemplarDirectory, samples);
        testTask.configure(task -> task.dependsOn(installTask));

        samples.configureEach(s -> {
            DefaultSample sample = (DefaultSample) s;

            DefaultExemplarExtension exemplar = project.getObjects().newInstance(DefaultExemplarExtension.class);
            s.getExtensions().add(ExemplarExtension.class, "exemplar", exemplar);
            exemplar.getSource().from(sample.getSampleDirectory());

            installTask.configure(task -> exemplar.actions.forEach(it -> it.execute(task)));
            installTask.configure(toOnlyInstallIfExemplarConfigurationArePresent(sample, samplesExemplarDirectory, project.getObjects()));
        });
    }

    private static void configureExemplarDependencies(DependencyHandler dependencies, SourceSet sourceSet) {
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.gradle:sample-check:0.7.0");
        dependencies.add(sourceSet.getImplementationConfigurationName(), dependencies.gradleTestKit());
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.slf4j:slf4j-simple:1.7.16");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "junit:junit:4.12");
    }

    private static Action<Sync> toOnlyInstallIfExemplarConfigurationArePresent(Sample sample, Provider<Directory> samplesDirectory, ObjectFactory objectFactory) {
        return (Sync task) -> {
            task.dependsOn(sample.getExtensions().getByType(ExemplarExtension.class).getSource());
            task.from(new Callable<FileCollection>() {
                @Override
                public FileCollection call() throws Exception {
                    if (exemplarConfigurationArePresent(sample)) {
                        return sample.getExtensions().getByType(ExemplarExtension.class).getSource();
                    }
                    return objectFactory.fileCollection();
                }
            }, spec -> spec.into(sample.getName()));
        };
    }

    private static TaskProvider<Task> createExemplarGeneratorTask(TaskContainer tasks, ProjectLayout projectLayout, SourceSet sourceSet) {
        Provider<Directory> testSourceSet = projectLayout.getBuildDirectory().dir("generated-source-sets/" + sourceSet.getName());
        return tasks.register("generate" + capitalize(sourceSet.getName() + "SourceSet"), task -> {
            task.getOutputs().dir(testSourceSet);
            task.doLast(it -> {
                String content = "//CHECKSTYLE:OFF\n"
                        + "package org.gradle.samples;\n"
                        + "\n"
                        + "import org.gradle.samples.test.normalizer.FileSeparatorOutputNormalizer;\n"
                        + "import org.gradle.samples.test.normalizer.JavaObjectSerializationOutputNormalizer;\n"
                        + "import org.gradle.samples.test.runner.GradleSamplesRunner;\n"
                        + "import org.gradle.samples.test.runner.SamplesOutputNormalizers;\n"
                        + "import org.gradle.samples.test.runner.SamplesRoot;\n"
                        + "import org.junit.runner.RunWith;\n"
                        + "\n"
                        + "@RunWith(GradleSamplesRunner.class)\n"
                        + "@SamplesOutputNormalizers({\n"
                        + "    JavaObjectSerializationOutputNormalizer.class,\n"
                        + "    FileSeparatorOutputNormalizer.class\n"
                        + "})\n"
                        + "public class ExemplarExternalSamplesFunctionalTest {}"
                        + "//CHECKSTYLE:ON\n";
                try {
                    Files.write(testSourceSet.map(f -> f.file("ExemplarExternalSamplesFunctionalTest.java")).get().getAsFile().toPath(), content.getBytes());
                } catch (IOException e) {
                    throw new UnsupportedOperationException(e);
                }
            });
        });
    }

    private static SourceSet createSourceSet(SourceSetContainer sourceSets) {
        return sourceSets.create("samplesExemplarFunctionalTest");
    }

    private static TaskProvider<Sync> createExemplarInstallTask(TaskContainer tasks, SourceSet sourceSet, Provider<Directory> samplesDirectory) {
        return tasks.register("install" + capitalize(sourceSet.getName()), Sync.class, task -> {
            task.into(samplesDirectory);
            task.setDestinationDir(samplesDirectory.get().getAsFile());
        });
    }

    private static boolean exemplarConfigurationArePresent(Sample sample) {
        return !sample.getExtensions().getByType(ExemplarExtension.class).getSource().getAsFileTree().matching(p -> p.include("*.sample.conf")).isEmpty();
    }

    private static TaskProvider<Test> createExemplarTestTask(TaskContainer tasks, SourceSet sourceSet, ProjectLayout projectLayout, Provider<Directory> samplesDirectory, NamedDomainObjectContainer<Sample> samples) {
        return tasks.register(sourceSet.getName(), Test.class, task -> {
            task.getInputs().dir(samplesDirectory).withPathSensitivity(PathSensitivity.RELATIVE);
            task.setTestClassesDirs(sourceSet.getRuntimeClasspath());
            task.setClasspath(sourceSet.getRuntimeClasspath());
            task.setWorkingDir(projectLayout.getProjectDirectory().getAsFile());
            task.systemProperty("integTest.samplesdir", samplesDirectory.get().getAsFile().getAbsolutePath());
            task.onlyIf(it -> samplesDirectory.get().getAsFile().exists());
//            task.onlyIf(it -> samples.stream().anyMatch(TestingSamplesWithExemplarPlugin::exemplarConfigurationArePresent));
        });
    }
}
