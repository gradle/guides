package org.gradle.docs.samples.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.samples.Dsl;
import org.gradle.docs.samples.SampleSummary;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.Template;
import org.gradle.docs.samples.internal.tasks.GenerateSampleIndexAsciidoc;
import org.gradle.docs.samples.internal.tasks.GenerateSanityCheckTests;
import org.gradle.docs.samples.internal.tasks.GenerateTestSource;
import org.gradle.docs.samples.internal.tasks.InstallSample;
import org.gradle.docs.samples.internal.tasks.SyncWithProvider;
import org.gradle.docs.samples.internal.tasks.ValidateSampleBinary;
import org.gradle.docs.samples.internal.tasks.ZipSample;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.StringUtils.*;
import static org.gradle.docs.samples.internal.SamplesDocumentationPlugin.toSummary;

@SuppressWarnings("UnstableApiUsage")
public class SamplesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ProviderFactory providers = project.getProviders();
        ObjectFactory objects = project.getObjects();

        project.getPluginManager().apply(SamplesDocumentationPlugin.class);
        project.getPluginManager().apply("java-base");

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.named("checkSamples");

        // Register a samples extension to configure published samples
        SamplesInternal extension = configureSamplesExtension(project, layout);

        // Samples
        FileCollection generatedTests = createGeneratedTests(tasks, objects, layout);
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, generatedTests, sample));

        // Samples binaries
        extension.getBinaries().withType(SampleArchiveBinary.class).all(binary -> createTasksForSampleBinary(tasks, layout, binary));

        // Templates
        extension.getTemplates().configureEach(template -> applyConventionsForTemplates(extension, template));
        extension.getTemplates().all(template -> createTasksForTemplates(layout, tasks, template));

        // Testing
        addExemplarTestsForSamples(project, layout, tasks, extension, check);
    }

    private FileCollection createGeneratedTests(TaskContainer tasks, ObjectFactory objects, ProjectLayout layout) {
        TaskProvider<GenerateSanityCheckTests> generateSanityCheckTests = tasks.register("generateSanityCheckTests", GenerateSanityCheckTests.class, task -> {
            task.setDescription("Generates exemplar configuration file needed to sanity check the sample (aka, run gradle help).");
            task.getOutputFile().convention(layout.getBuildDirectory().file("tmp/" + task.getName() + "/sanityCheck.sample.conf"));
        });
        ConfigurableFileCollection generatedFiles = objects.fileCollection();
        generatedFiles.from(generateSanityCheckTests);
        return generatedFiles;
    }

    private void createTasksForTemplates(ProjectLayout layout, TaskContainer tasks, Template template) {
        TaskProvider<SyncWithProvider> generateTemplate = tasks.register("generateTemplate" + capitalize(template.getName()), SyncWithProvider.class, task -> {
            task.setDescription("Generates template into a target directory.");
            task.from(template.getSourceDirectory(), copySpec -> copySpec.into(template.getTarget()));
            task.into(layout.getBuildDirectory().dir("tmp/" + task.getName()));
        });

        template.getTemplateDirectory().convention(generateTemplate.flatMap(SyncWithProvider::getDestinationDirectory));
    }

    private void applyConventionsForTemplates(Samples extension, Template template) {
        template.getTarget().convention("");
        template.getSourceDirectory().convention(extension.getTemplatesRoot().dir(toKebabCase(template.getName())));
    }

    private void addExemplarTestsForSamples(Project project, ProjectLayout layout, TaskContainer tasks, SamplesInternal extension, TaskProvider<Task> check) {
        SourceSet sourceSet = project.getExtensions().getByType(SourceSetContainer.class).create("samplesExemplarFunctionalTest");
        TaskProvider<GenerateTestSource> generatorTask = createExemplarGeneratorTask(tasks, layout, sourceSet);
        sourceSet.getJava().srcDir(generatorTask.flatMap(GenerateTestSource::getOutputDirectory));

        DependencyHandler dependencies = project.getDependencies();
        dependencies.add(sourceSet.getImplementationConfigurationName(), dependencies.gradleTestKit());
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.gradle:sample-check:0.9.0");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.slf4j:slf4j-simple:1.7.16");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "junit:junit:4.12");

        TaskProvider<Test> exemplarTest = createExemplarTestTask(tasks, sourceSet, layout, extension);
        // TODO: Make the sample's check task depend on this test?
        check.configure(task -> task.dependsOn(exemplarTest));
    }

    private SamplesInternal configureSamplesExtension(Project project, ProjectLayout layout) {
        SamplesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSamples();

        project.getExtensions().add(Samples.class, "samples", extension);
        extension.getSamplesRoot().set(layout.getProjectDirectory().dir("src/samples"));

        extension.getTemplatesRoot().convention(layout.getProjectDirectory().dir("src/samples/templates"));
        extension.getCommonExcludes().convention(Arrays.asList("**/build/**", "**/.gradle/**"));

        extension.getTestedInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/testing"));
        extension.getDistribution().getTestedInstalledSamples().from(extension.getTestedInstallRoot());
        extension.getDistribution().getTestedInstalledSamples().builtBy(extension.getDistribution().getInstalledSamples().builtBy((Callable<List<DirectoryProperty>>) () -> extension.getBinaries().withType(SampleArchiveBinary.class).stream().map(SampleArchiveBinary::getTestedInstallDirectory).collect(Collectors.toList())));

        return extension;
    }

    private void applyConventionsForSamples(SamplesInternal extension, FileCollection generatedTests, SampleInternal sample) {
        String name = sample.getName();
        // Converts names like androidApplication to android-application
        sample.getTestedInstallDirectory().convention(extension.getTestedInstallRoot().dir(toKebabCase(name)));

        sample.tests(content -> {
            content.from(sample.getSampleDirectory().dir("tests"));
            content.from(generatedTests);
        });
    }

    private void createTasksForSampleBinary(TaskContainer tasks, ProjectLayout layout, SampleArchiveBinary binary) {
        TaskProvider<InstallSample> installSampleTaskForTesting = tasks.register("installSample" + capitalize(binary.getName()) + "ForTest", InstallSample.class, task -> {
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory with testing support.");
            // TODO: zipTree should be lazy
            task.dependsOn(binary.getZipFile());
            task.getSource().from((Callable<FileTree>)() -> task.getProject().zipTree(binary.getZipFile()));
            task.getSource().from(binary.getTestsContent());
            task.getInstallDirectory().convention(binary.getTestedWorkingDirectory());
        });
        binary.getTestedInstallDirectory().convention(installSampleTaskForTesting.flatMap(InstallSample::getInstallDirectory));
    }

    private static TaskProvider<GenerateTestSource> createExemplarGeneratorTask(TaskContainer tasks, ProjectLayout layout, SourceSet sourceSet) {
        return tasks.register("generate" + capitalize(sourceSet.getName() + "SourceSet"), GenerateTestSource.class, task -> {
            task.setDescription("Generates source file to run exemplar tests for published samples.");
            task.getOutputDirectory().convention(layout.getBuildDirectory().dir("generated-sources/" + sourceSet.getName()));
        });
    }

    private static TaskProvider<Test> createExemplarTestTask(TaskContainer tasks, SourceSet sourceSet, ProjectLayout layout, SamplesInternal extension) {
        DirectoryProperty samplesDirectory = extension.getTestedInstallRoot();

        return tasks.register(sourceSet.getName(), Test.class, task -> {
            task.setGroup("verification");
            task.setDescription("Test samples.");
            task.getInputs().dir(samplesDirectory).withPathSensitivity(PathSensitivity.RELATIVE);
            task.setTestClassesDirs(sourceSet.getRuntimeClasspath());
            task.setClasspath(sourceSet.getRuntimeClasspath());
            task.setWorkingDir(layout.getProjectDirectory().getAsFile());
            // TODO: This isn't lazy.  Need a different API here.
            task.systemProperty("integTest.samplesdir", samplesDirectory.get().getAsFile().getAbsolutePath());
            task.dependsOn(extension.getDistribution().getTestedInstalledSamples());
        });
    }
}
