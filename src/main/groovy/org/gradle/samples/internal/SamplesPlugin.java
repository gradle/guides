package org.gradle.samples.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.samples.Dsl;
import org.gradle.samples.Sample;
import org.gradle.samples.SampleBinary;
import org.gradle.samples.SamplesExtension;
import org.gradle.samples.Template;
import org.gradle.samples.internal.tasks.GenerateSampleIndexAsciidoc;
import org.gradle.samples.internal.tasks.GenerateSamplePageAsciidoc;
import org.gradle.samples.internal.tasks.GenerateTestSource;
import org.gradle.samples.internal.tasks.InstallSample;
import org.gradle.samples.internal.tasks.SyncWithProvider;
import org.gradle.samples.internal.tasks.ValidateSampleBinary;
import org.gradle.samples.internal.tasks.ZipSample;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.samples.internal.StringUtils.*;

public class SamplesPlugin implements Plugin<Project> {
    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ObjectFactory objectFactory = project.getObjects();

        project.getPluginManager().apply("java-base");

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME);

        // Register a samples extension to configure published samples
        SamplesExtension extension = createSamplesExtension(project, layout);

        // Samples
        // Generate wrapper files that can be shared by all samples
        FileTree wrapperFiles = createWrapperFiles(tasks, objectFactory);
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, wrapperFiles, sample));

        // Sample binaries
        // Create tasks for each sample binary (a sample for a particular DSL)
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().all(binary -> createTasksForSampleBinary(tasks, layout, binary));

        // Documentation for sample index
        registerGenerateSampleIndex(tasks, extension);
        TaskProvider<Sync> assembleDocs = tasks.register("assembleSampleDocs", Sync.class, task -> {
            task.from(extension.getSampleIndexFile());
            task.from((Callable<List<RegularFileProperty>>) () -> extension.getPublishedSamples().stream().map(Sample::getSamplePageFile).collect(Collectors.toList()));
            task.from((Callable<List<RegularFileProperty>>) () -> extension.getBinaries().stream().map(SampleBinary::getZipFile).collect(Collectors.toList()), copySpec -> copySpec.into("zips"));
            task.into(extension.getDocumentationRoot());
        });
        extension.getAssembledDocumentation().from(assembleDocs);
        assemble.configure(t -> t.dependsOn(extension.getAssembledDocumentation()));

        // Templates
        extension.getTemplates().configureEach(template -> applyConventionsForTemplates(extension, template));
        extension.getTemplates().all(template -> createTasksForTemplates(layout, tasks, template));

        // Testing
        addExemplarTestsForSamples(project, layout, tasks, extension, check);

        // Trigger everything by realizing sample container
        project.afterEvaluate(p -> realizeSamples(tasks, extension, assemble, check));
    }

    private void createTasksForTemplates(ProjectLayout layout, TaskContainer tasks, Template template) {
        TaskProvider<SyncWithProvider> generateTemplate = tasks.register("generateTemplate" + capitalize(template.getName()), SyncWithProvider.class, task -> {
            task.from(template.getSourceDirectory(), copySpec -> {
                copySpec.into(template.getTarget());
            });
            task.into(layout.getBuildDirectory().dir("tmp/" + task.getName()));
        });

        template.getTemplateDirectory().convention(generateTemplate.flatMap(task -> task.getDestinationDirectory()));
    }

    private void applyConventionsForTemplates(SamplesExtension extension, Template template) {
        template.getTarget().convention("");
        template.getSourceDirectory().convention(extension.getTemplatesRoot().dir(toKebabCase(template.getName())));
    }

    private void addExemplarTestsForSamples(Project project, ProjectLayout layout, TaskContainer tasks, SamplesExtension extension, TaskProvider<Task> check) {
        SourceSet sourceSet = project.getExtensions().getByType(SourceSetContainer.class).create("samplesExemplarFunctionalTest");
        TaskProvider<GenerateTestSource> generatorTask = createExemplarGeneratorTask(tasks, layout, sourceSet);
        sourceSet.getJava().srcDir(generatorTask.flatMap(task -> task.getOutputDirectory()));

        DependencyHandler dependencies = project.getDependencies();
        dependencies.add(sourceSet.getImplementationConfigurationName(), dependencies.gradleTestKit());
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.gradle:sample-check:0.9.0");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.slf4j:slf4j-simple:1.7.16");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "junit:junit:4.12");

        TaskProvider<Test> exemplarTest = createExemplarTestTask(tasks, sourceSet, layout, extension);
        // TODO: Make the sample's check task depend on this test?
        check.configure(task -> task.dependsOn(exemplarTest));
    }

    private SamplesExtension createSamplesExtension(Project project, ProjectLayout layout) {
        SamplesExtension extension = project.getExtensions().create(SamplesExtension.class, "samples", DefaultSamplesExtension.class);
        extension.getSamplesRoot().convention(layout.getProjectDirectory().dir("src/samples"));
        extension.getTemplatesRoot().convention(layout.getProjectDirectory().dir("src/samples/templates"));
        extension.getInstallRoot().convention(layout.getBuildDirectory().dir("install/samples"));
        extension.getDocumentationRoot().convention(layout.getBuildDirectory().dir("samples/docs/"));
        extension.getCommonExcludes().convention(Arrays.asList("**/build/**", "**/.gradle/**"));
        return extension;
    }

    private void registerGenerateSampleIndex(TaskContainer tasks, SamplesExtension extension) {
        TaskProvider<GenerateSampleIndexAsciidoc> generateSampleIndex = tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.getSamples().convention(extension.getPublishedSamples());
            // TODO: This ignores changes to the temporary directory
            task.getOutputFile().set(new File(task.getTemporaryDir(), "index_samples.adoc"));
        });
        extension.getSampleIndexFile().convention(generateSampleIndex.flatMap(GenerateSampleIndexAsciidoc::getOutputFile));
    }

    private FileTree createWrapperFiles(TaskContainer tasks, ObjectFactory objectFactory) {
        TaskProvider<Wrapper> wrapper = tasks.register("generateWrapperForSamples", Wrapper.class, task -> {
            // TODO: This ignores changes to the temporary directory
            task.setJarFile(new File(task.getTemporaryDir(), "gradle/wrapper/gradle-wrapper.jar"));
            task.setScriptFile(new File(task.getTemporaryDir(), "gradlew"));
        });
        ConfigurableFileCollection wrapperFiles = objectFactory.fileCollection();
        wrapperFiles.from(wrapper.map(AbstractTask::getTemporaryDir));
        wrapperFiles.builtBy(wrapper);
        return wrapperFiles.getAsFileTree();
    }

    private void registerGenerateSamplePage(TaskContainer tasks, Sample sample) {
        TaskProvider<GenerateSamplePageAsciidoc> generateSamplePage = tasks.register("generate" + capitalize(sample.getName()) + "Page", GenerateSamplePageAsciidoc.class, task -> {
            task.getReadmeFile().convention(sample.getReadMeFile());
            // TODO: This ignores changes to the temporary directory
            task.getOutputFile().set(new File(task.getTemporaryDir(), "sample_" + sample.getName() + ".adoc"));
            task.setDescription("Generates asciidoc page for sample '" + sample.getName() + "'");
        });
        sample.getSamplePageFile().convention(generateSamplePage.flatMap(GenerateSamplePageAsciidoc::getOutputFile));
    }

    private void applyConventionsForSamples(SamplesExtension extension, FileTree wrapperFiles, Sample sample) {
        String name = sample.getName();
        sample.getDisplayName().convention(toTitleCase(name));
        // Converts names like androidApplication to android-application
        sample.getSampleDirectory().convention(extension.getSamplesRoot().dir(toKebabCase(name)));
        sample.getInstallDirectory().convention(extension.getInstallRoot().dir(name));
        sample.getDescription().convention("");

        sample.getLicenseFile().convention(sample.getSampleDirectory().file("LICENSE"));
        sample.getReadMeFile().convention(sample.getSampleDirectory().file("README.adoc"));

        sample.getDsls().convention(Arrays.asList(Dsl.values()));
        sample.common(content -> {
            content.from(sample.getLicenseFile());
            content.from(sample.getSamplePageFile());
            content.from(sample.getSampleDirectory().dir("tests"));
            content.from(wrapperFiles);
        });
        sample.groovy(content -> content.from(sample.getSampleDirectory().dir(Dsl.GROOVY.getConventionalDirectory())));
        sample.kotlin(content -> content.from(sample.getSampleDirectory().dir(Dsl.KOTLIN.getConventionalDirectory())));

        sample.getAssembleTask().configure(task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample.");
        });
        sample.getCheckTask().configure(task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Checks '" + sample.getName() + "' sample.");
        });
    }

    private Provider<SampleBinary> registerSampleBinaryForDsl(SamplesExtension extension, Sample sample, Dsl dsl) {
        return extension.getBinaries().register(sample.getName() + dsl.getDisplayName(), binary -> {
            binary.getDsl().convention(dsl).disallowChanges();
            binary.getSamplePageFile().convention(sample.getSamplePageFile()).disallowChanges();
            switch (dsl) {
                case GROOVY:
                    binary.getWorkingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
                    binary.getDslSpecificContent().from(sample.getGroovyContent()).disallowChanges();
                    break;
                case KOTLIN:
                    binary.getWorkingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
                    binary.getDslSpecificContent().from(sample.getKotlinContent()).disallowChanges();
                    break;
                default:
                    throw new GradleException("Unhandled Dsl type " + dsl + " for sample '" + sample.getName() + "'");
            }
            binary.getExcludes().convention(extension.getCommonExcludes());
            binary.getContent().from(sample.getCommonContent());
            binary.getContent().from(binary.getDslSpecificContent());
            binary.getContent().disallowChanges();
        });
    }

    private void createTasksForSampleBinary(TaskContainer tasks, ProjectLayout layout, SampleBinary binary) {
        TaskProvider<ValidateSampleBinary> validateSample = tasks.register("validateSample" + capitalize(binary.getName()), ValidateSampleBinary.class, task -> {
            task.getDsl().convention(binary.getDsl());
            task.getSampleName().convention(binary.getName());
            task.getReadmeName().convention(binary.getSamplePageFile().map(f -> f.getAsFile().getName()));
            task.getZipFile().convention(binary.getZipFile());
            task.getReportFile().convention(layout.getBuildDirectory().file("reports/sample-validation/" + task.getName() + ".txt"));
            task.setDescription("Checks the sample '" + binary.getName() + "' is valid.");
        });
        binary.getValidationReport().convention(validateSample.flatMap(task -> task.getReportFile()));

        TaskProvider<InstallSample> installSampleTask = tasks.register("installSample" + capitalize(binary.getName()), InstallSample.class, task -> {
            task.getSource().from(binary.getZipFile());
            task.getInstallDirectory().convention(binary.getWorkingDirectory());
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory.");
        });
        binary.getInstallDirectory().convention(installSampleTask.flatMap(InstallSample::getInstallDirectory));

        TaskProvider<ZipSample> zipTask = tasks.register("zipSample" + capitalize(binary.getName()), ZipSample.class, task -> {
            task.getSource().from(binary.getContent());
            task.getMainSource().from(binary.getDslSpecificContent());
            task.getArchiveFile().convention(layout.getBuildDirectory().file("sample-zips/" + binary.getName() + ".zip"));
            task.getExcludes().convention(binary.getExcludes());
            task.setDescription("Creates a zip for sample '" + binary.getName() + "'.");
        });
        binary.getZipFile().convention(zipTask.flatMap(ZipSample::getArchiveFile));
    }

    private static TaskProvider<GenerateTestSource> createExemplarGeneratorTask(TaskContainer tasks, ProjectLayout layout, SourceSet sourceSet) {
        return tasks.register("generate" + capitalize(sourceSet.getName() + "SourceSet"), GenerateTestSource.class, task -> {
            task.getOutputDirectory().convention(layout.getBuildDirectory().dir("generated-sources/" + sourceSet.getName()));
        });
    }

    private static TaskProvider<Test> createExemplarTestTask(TaskContainer tasks, SourceSet sourceSet, ProjectLayout layout, SamplesExtension extension) {
        return tasks.register(sourceSet.getName(), Test.class, task -> {
            DirectoryProperty samplesDirectory = extension.getInstallRoot();
            task.getInputs().dir(samplesDirectory).withPathSensitivity(PathSensitivity.RELATIVE);
            task.setTestClassesDirs(sourceSet.getRuntimeClasspath());
            task.setClasspath(sourceSet.getRuntimeClasspath());
            task.setWorkingDir(layout.getProjectDirectory().getAsFile());
            // TODO: This isn't lazy.  Need a different API here.
            task.systemProperty("integTest.samplesdir", samplesDirectory.get().getAsFile().getAbsolutePath());
            task.dependsOn((Callable<Object[]>) () -> extension.getBinaries().stream().map(SampleBinary::getInstallDirectory).toArray());
        });
    }

    private void realizeSamples(TaskContainer tasks, SamplesExtension extension, TaskProvider<Task> assemble, TaskProvider<Task> check) {
        // TODO: Disallow changes to published samples container after this point.
        for (Sample sample : extension.getPublishedSamples()) {
            // TODO: To make this lazy without afterEvaluate/eagerness, we need to be able to tell the tasks container that the samples container should be consulted
            assemble.configure(t -> t.dependsOn(sample.getAssembleTask()));
            check.configure(t -> t.dependsOn(sample.getCheckTask()));
            registerGenerateSamplePage(tasks, sample);

            sample.getDsls().disallowChanges();
            // TODO: This should only be enforced if we are trying to build the given sample
            List<Dsl> dsls = sample.getDsls().get();
            if (dsls.isEmpty()) {
                throw new GradleException("Samples must have at least one DSL, sample '" + sample.getName() + "' has none.");
            }
            for (Dsl dsl : dsls) {
                Provider<SampleBinary> binary = registerSampleBinaryForDsl(extension, sample, dsl);
                sample.getAssembleTask().configure(task -> task.dependsOn(binary.flatMap(SampleBinary::getZipFile)));
                sample.getCheckTask().configure(task -> task.dependsOn(binary.flatMap(SampleBinary::getValidationReport)));
            }
        }
    }
}
