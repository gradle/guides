package org.gradle.docs.samples.internal;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.docs.samples.internal.tasks.GenerateSampleIndexAsciidoc;
import org.gradle.docs.samples.internal.tasks.GenerateSamplePageAsciidoc;
import org.gradle.docs.samples.internal.tasks.GenerateSanityCheckTests;
import org.gradle.docs.samples.internal.tasks.GenerateTestSource;
import org.gradle.docs.samples.internal.tasks.InstallSample;
import org.gradle.docs.samples.internal.tasks.SyncWithProvider;
import org.gradle.docs.samples.internal.tasks.ValidateSampleBinary;
import org.gradle.docs.samples.internal.tasks.ZipSample;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.docs.samples.Dsl;
import org.gradle.docs.samples.Sample;
import org.gradle.docs.samples.SampleBinary;
import org.gradle.docs.samples.SampleSummary;
import org.gradle.docs.samples.SamplesExtension;
import org.gradle.docs.samples.Template;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.StringUtils.*;

@SuppressWarnings("UnstableApiUsage")
public class SamplesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ProviderFactory providers = project.getProviders();
        ObjectFactory objects = project.getObjects();

        project.getPluginManager().apply("java-base");
        project.getPluginManager().apply("org.asciidoctor.convert");

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.register("checkSamples");

        // Register a samples extension to configure published samples
        SamplesExtension extension = createSamplesExtension(project, layout);

        // Samples
        // Generate wrapper files that can be shared by all samples
        FileTree wrapperFiles = createWrapperFiles(tasks, objects);
        FileCollection generatedTests = createGeneratedTests(tasks, objects, layout);
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, wrapperFiles, generatedTests, sample));

        // Sample binaries
        // Create tasks for each sample binary (a sample for a particular DSL)
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().all(binary -> createTasksForSampleBinary(tasks, layout, binary));

        // Documentation for sample index
        registerGenerateSampleIndex(tasks, providers, objects, extension);

        // Render all the documentation out to HTML
        renderSamplesDocumentation(tasks, assemble, extension);

        // Templates
        extension.getTemplates().configureEach(template -> applyConventionsForTemplates(extension, template));
        extension.getTemplates().all(template -> createTasksForTemplates(layout, tasks, template));

        // Testing
        addExemplarTestsForSamples(project, layout, tasks, extension, check);

        // Trigger everything by realizing sample container
        project.afterEvaluate(p -> realizeSamples(tasks, objects, extension, assemble, check));
    }

    private void renderSamplesDocumentation(TaskContainer tasks, TaskProvider<Task> assemble, SamplesExtension extension) {
        TaskProvider<Sync> assembleDocs = tasks.register("assembleSamples", Sync.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Assembles all intermediate files needed to generate the samples documentation.");
            task.from(extension.getSampleIndexFile());
            // TODO: File collection of the readmes too?
            task.from((Callable<List<RegularFileProperty>>) () -> extension.getPublishedSamples().stream().map(Sample::getSamplePageFile).collect(Collectors.toList()));
            task.from(extension.getDistribution().getZippedSamples(), sub -> sub.into("zips"));
            task.from(extension.getDistribution().getInstalledSamples(), sub -> sub.into("samples"));
            task.into(extension.getDocumentationInstallRoot());
        });

        TaskProvider<AsciidoctorTask> samplesMultiPage = tasks.register("samplesMultiPage", AsciidoctorTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generates multi-page samples index.");
            task.dependsOn(assembleDocs);

            task.sources(new Closure(null) {
                public Object doCall(Object ignore) {
                    ((PatternSet)this.getDelegate()).include("**/*.adoc");
                    ((PatternSet)this.getDelegate()).exclude("samples/**/*.adoc");
                    return null;
                }
            });
            // TODO: This breaks the provider
            task.setSourceDir(extension.getDocumentationInstallRoot().get().getAsFile());
            // TODO: This breaks the provider
            task.setOutputDir(extension.getRenderedDocumentationRoot().get().getAsFile());
            task.resources(new Closure(task) {
                public Object doCall(Object ignore) {
                    ((CopySpec)this.getDelegate()).from(extension.getDistribution().getZippedSamples(), sub -> sub.into("zips"));
                    return this.getDelegate();
                }
            });

            task.setSeparateOutputDirs(false);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("doctype", "book");
            attributes.put("icons", "font");
            attributes.put("source-highlighter", "prettify");
            attributes.put("toc", "auto");
            attributes.put("toclevels", 1);
            attributes.put("toc-title", "Contents");
            // TODO: This is specific to gradle/gradle
            attributes.put("userManualPath", "../userguide");
            attributes.put("samples-dir", extension.getDocumentationInstallRoot().get().getAsFile());
            task.attributes(attributes);
        });
        extension.getDistribution().getRenderedDocumentation().from(samplesMultiPage);

        assemble.configure(t -> t.dependsOn(extension.getDistribution().getRenderedDocumentation()));
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

    private void applyConventionsForTemplates(SamplesExtension extension, Template template) {
        template.getTarget().convention("");
        template.getSourceDirectory().convention(extension.getTemplatesRoot().dir(toKebabCase(template.getName())));
    }

    private void addExemplarTestsForSamples(Project project, ProjectLayout layout, TaskContainer tasks, SamplesExtension extension, TaskProvider<Task> check) {
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

    private SamplesExtension createSamplesExtension(Project project, ProjectLayout layout) {
        SamplesExtension extension = project.getExtensions().create(SamplesExtension.class, "samples", DefaultSamplesExtension.class);
        extension.getSamplesRoot().convention(layout.getProjectDirectory().dir("src/samples"));
        extension.getTemplatesRoot().convention(layout.getProjectDirectory().dir("src/samples/templates"));
        extension.getDocumentationInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/docs/"));
        extension.getCommonExcludes().convention(Arrays.asList("**/build/**", "**/.gradle/**"));

        extension.getInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/install"));
        extension.getDistribution().getInstalledSamples().from(extension.getInstallRoot());
        extension.getDistribution().getInstalledSamples().builtBy((Callable<List<DirectoryProperty>>) () -> extension.getBinaries().stream().map(SampleBinary::getInstallDirectory).collect(Collectors.toList()));
        extension.getDistribution().getZippedSamples().from((Callable<List<RegularFileProperty>>) () -> extension.getBinaries().stream().map(SampleBinary::getZipFile).collect(Collectors.toList()));

        extension.getTestedInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/testing"));
        extension.getDistribution().getTestedInstalledSamples().from(extension.getTestedInstallRoot());
        extension.getDistribution().getTestedInstalledSamples().builtBy(extension.getDistribution().getInstalledSamples().builtBy((Callable<List<DirectoryProperty>>) () -> extension.getBinaries().stream().map(SampleBinary::getTestedInstallDirectory).collect(Collectors.toList())));

        extension.getRenderedDocumentationRoot().convention(layout.getBuildDirectory().dir("working/samples/render-samples"));
        return extension;
    }

    private void registerGenerateSampleIndex(TaskContainer tasks, ProviderFactory providers, ObjectFactory objects, SamplesExtension extension) {
        TaskProvider<GenerateSampleIndexAsciidoc> generateSampleIndex = tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generate index page that contains all published samples.");
            task.getSamples().convention(providers.provider(() -> extension.getPublishedSamples().stream().map(sample -> toSummary(objects, sample)).collect(Collectors.toSet())));
            // TODO: This ignores changes to the temporary directory
            task.getOutputFile().set(new File(task.getTemporaryDir(), "index.adoc"));
        });
        extension.getSampleIndexFile().convention(generateSampleIndex.flatMap(GenerateSampleIndexAsciidoc::getOutputFile));
    }

    private FileTree createWrapperFiles(TaskContainer tasks, ObjectFactory objectFactory) {
        TaskProvider<Wrapper> wrapper = tasks.register("generateWrapperForSamples", Wrapper.class, task -> {
            task.setDescription("Generates wrapper for samples.");
            // TODO: This ignores changes to the temporary directory
            task.setJarFile(new File(task.getTemporaryDir(), "gradle/wrapper/gradle-wrapper.jar"));
            task.setScriptFile(new File(task.getTemporaryDir(), "gradlew"));
        });
        ConfigurableFileCollection wrapperFiles = objectFactory.fileCollection();
        wrapperFiles.from(wrapper.map(AbstractTask::getTemporaryDir));
        wrapperFiles.builtBy(wrapper);
        return wrapperFiles.getAsFileTree();
    }

    private void registerGenerateSamplePage(TaskContainer tasks, ObjectFactory objects, Sample sample) {
        TaskProvider<GenerateSamplePageAsciidoc> generateSamplePage = tasks.register("generate" + capitalize(sample.getName()) + "Page", GenerateSamplePageAsciidoc.class, task -> {
            task.setDescription("Generates asciidoc page for sample '" + sample.getName() + "'");
            task.getSampleSummary().convention(toSummary(objects, sample));
            task.getReadmeFile().convention(sample.getReadMeFile());
            task.getOutputFile().fileProvider(sample.getSampleDocName().map(fileName -> new File(task.getTemporaryDir(), fileName + ".adoc")));
        });
        sample.getSamplePageFile().convention(generateSamplePage.flatMap(GenerateSamplePageAsciidoc::getOutputFile));
    }

    private void applyConventionsForSamples(SamplesExtension extension, FileTree wrapperFiles, FileCollection generatedTests, Sample sample) {
        String name = sample.getName();
        sample.getDisplayName().convention(toTitleCase(name));
        // Converts names like androidApplication to android-application
        sample.getSampleDirectory().convention(extension.getSamplesRoot().dir(toKebabCase(name)));
        sample.getInstallDirectory().convention(extension.getInstallRoot().dir(toKebabCase(name)));
        sample.getTestedInstallDirectory().convention(extension.getTestedInstallRoot().dir(toKebabCase(name)));
        sample.getSampleDocName().convention("sample_" + toSnakeCase(name));
        sample.getDescription().convention("");
        sample.getCategory().convention("Uncategorized");

        sample.getLicenseFile().convention(sample.getSampleDirectory().file("LICENSE"));
        sample.getReadMeFile().convention(sample.getSampleDirectory().file("README.adoc"));

        sample.getDsls().convention(sample.getSampleDirectory().map(sampleDirectory -> {
            List<Dsl> dsls = new ArrayList<>();
            for (Dsl dsl : Dsl.values()) {
                if (sampleDirectory.dir(dsl.getConventionalDirectory()).getAsFile().exists()) {
                    dsls.add(dsl);
                }
            }
            return dsls;
        }));

        sample.tests(content -> {
            content.from(sample.getSampleDirectory().dir("tests"));
            content.from(generatedTests);
        });
        sample.common(content -> {
            content.from(sample.getLicenseFile());
            content.from(sample.getSamplePageFile());
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
            binary.getSampleLinkName().convention(sample.getSampleDocName()).disallowChanges();
            binary.getWorkingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
            binary.getTestedWorkingDirectory().convention(sample.getTestedInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
            switch (dsl) {
                case GROOVY:
                    binary.getDslSpecificContent().from(sample.getGroovyContent()).disallowChanges();
                    break;
                case KOTLIN:
                    binary.getDslSpecificContent().from(sample.getKotlinContent()).disallowChanges();
                    break;
                default:
                    throw new GradleException("Unhandled Dsl type " + dsl + " for sample '" + sample.getName() + "'");
            }
            binary.getExcludes().convention(extension.getCommonExcludes());
            binary.getContent().from(sample.getCommonContent());
            binary.getContent().from(binary.getDslSpecificContent());
            binary.getContent().disallowChanges();

            binary.getTestsContent().from(sample.getTestsContent());
        });
    }

    private void createTasksForSampleBinary(TaskContainer tasks, ProjectLayout layout, SampleBinary binary) {
        TaskProvider<ValidateSampleBinary> validateSample = tasks.register("validateSample" + capitalize(binary.getName()), ValidateSampleBinary.class, task -> {
            task.setDescription("Checks the sample '" + binary.getName() + "' is valid.");
            task.getDsl().convention(binary.getDsl());
            task.getSampleName().convention(binary.getName());
            task.getZipFile().convention(binary.getZipFile());
            task.getReportFile().convention(layout.getBuildDirectory().file("reports/sample-validation/" + task.getName() + ".txt"));
        });
        binary.getValidationReport().convention(validateSample.flatMap(ValidateSampleBinary::getReportFile));

        TaskProvider<InstallSample> installSampleTask = tasks.register("installSample" + capitalize(binary.getName()), InstallSample.class, task -> {
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory.");
            // TODO: zipTree should be lazy
            task.dependsOn(binary.getZipFile());
            task.getSource().from((Callable<FileTree>)() -> task.getProject().zipTree(binary.getZipFile()));
            task.getInstallDirectory().convention(binary.getWorkingDirectory());
        });
        binary.getInstallDirectory().convention(installSampleTask.flatMap(InstallSample::getInstallDirectory));

        TaskProvider<InstallSample> installSampleTaskForTesting = tasks.register("installSample" + capitalize(binary.getName()) + "ForTest", InstallSample.class, task -> {
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory with testing support.");
            // TODO: zipTree should be lazy
            task.dependsOn(binary.getZipFile());
            task.getSource().from((Callable<FileTree>)() -> task.getProject().zipTree(binary.getZipFile()));
            task.getSource().from(binary.getTestsContent());
            task.getInstallDirectory().convention(binary.getTestedWorkingDirectory());
        });
        binary.getTestedInstallDirectory().convention(installSampleTaskForTesting.flatMap(InstallSample::getInstallDirectory));

        TaskProvider<ZipSample> zipTask = tasks.register("zipSample" + capitalize(binary.getName()), ZipSample.class, task -> {
            task.setDescription("Creates a zip for sample '" + binary.getName() + "'.");
            task.getSource().from(binary.getContent());
            task.getMainSource().from(binary.getDslSpecificContent());
            task.getReadmeName().convention(binary.getSampleLinkName().map(name -> name + ".adoc"));
            task.getExcludes().convention(binary.getExcludes());
            // TODO: make this relocatable too?
            task.getArchiveFile().convention(layout.getBuildDirectory().file(binary.getSampleLinkName().map(name -> String.format("sample-zips/%s-%s.zip", name, binary.getDsl().get().getDslLabel()))));
        });
        binary.getZipFile().convention(zipTask.flatMap(ZipSample::getArchiveFile));
    }

    private static TaskProvider<GenerateTestSource> createExemplarGeneratorTask(TaskContainer tasks, ProjectLayout layout, SourceSet sourceSet) {
        return tasks.register("generate" + capitalize(sourceSet.getName() + "SourceSet"), GenerateTestSource.class, task -> {
            task.setDescription("Generates source file to run exemplar tests for published samples.");
            task.getOutputDirectory().convention(layout.getBuildDirectory().dir("generated-sources/" + sourceSet.getName()));
        });
    }

    private static TaskProvider<Test> createExemplarTestTask(TaskContainer tasks, SourceSet sourceSet, ProjectLayout layout, SamplesExtension extension) {
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

    private void realizeSamples(TaskContainer tasks, ObjectFactory objects, SamplesExtension extension, TaskProvider<Task> assemble, TaskProvider<Task> check) {
        // TODO: Disallow changes to published samples container after this point.
        for (Sample sample : extension.getPublishedSamples()) {
            if (sample.getName().contains("_") || sample.getName().contains("-")) {
                throw new IllegalArgumentException(String.format("Sample '%s' has disallowed characters", sample.getName()));
            }
            // TODO: To make this lazy without afterEvaluate/eagerness, we need to be able to tell the tasks container that the samples container should be consulted
            assemble.configure(t -> t.dependsOn(sample.getAssembleTask()));
            check.configure(t -> t.dependsOn(sample.getCheckTask()));
            registerGenerateSamplePage(tasks, objects, sample);

            sample.getDsls().disallowChanges();
            // TODO: This should only be enforced if we are trying to build the given sample
            Set<Dsl> dsls = sample.getDsls().get();
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

    private SampleSummary toSummary(ObjectFactory objects, Sample sample) {
        SampleSummary summary = objects.newInstance(SampleSummary.class);
        summary.getDisplayName().set(sample.getDisplayName());
        summary.getDsls().set(sample.getDsls());
        summary.getCategory().set(sample.getCategory());
        summary.getDescription().set(sample.getDescription());
        summary.getSampleDocName().set(sample.getSampleDocName());
        return summary;
    }
}
