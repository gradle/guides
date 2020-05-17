package org.gradle.docs.samples.internal;

import groovy.lang.Closure;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
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
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.samples.Dsl;
import org.gradle.docs.samples.SampleSummary;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.Template;
import org.gradle.docs.samples.internal.tasks.GenerateSampleIndexAsciidoc;
import org.gradle.docs.samples.internal.tasks.GenerateSanityCheckTests;
import org.gradle.docs.samples.internal.tasks.GenerateTestSource;
import org.gradle.docs.samples.internal.tasks.InstallSample;
import org.gradle.docs.samples.internal.tasks.LockReleasingAsciidoctorTask;
import org.gradle.docs.samples.internal.tasks.SamplesReport;
import org.gradle.docs.samples.internal.tasks.SyncWithProvider;
import org.gradle.docs.samples.internal.tasks.ValidateSampleBinary;
import org.gradle.docs.samples.internal.tasks.ZipSample;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.Asserts.assertNameDoesNotContainsDisallowedCharacters;
import static org.gradle.docs.internal.DocumentationBasePlugin.DOCS_TEST_SOURCE_SET_NAME;
import static org.gradle.docs.internal.DocumentationBasePlugin.DOCUMENTATION_GROUP_NAME;
import static org.gradle.docs.internal.StringUtils.*;
import static org.gradle.docs.internal.configure.AsciidoctorTasks.*;
import static org.gradle.docs.internal.configure.ContentBinaries.*;

@SuppressWarnings("UnstableApiUsage")
public class SamplesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ProviderFactory providers = project.getProviders();
        ObjectFactory objects = project.getObjects();

        project.getPluginManager().apply(DocumentationBasePlugin.class);
        project.getPluginManager().apply("org.asciidoctor.convert"); // For the `asciidoctor` configuration

        Configuration asciidoctorConfiguration = project.getConfigurations().maybeCreate("asciidoctorForDocumentation");
        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getDependencies().add(asciidoctorConfiguration.getName(), "org.gradle:docs-asciidoctor-extensions:0.8.0");
        project.getConfigurations().getByName("asciidoctor").extendsFrom(asciidoctorConfiguration);

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.register("checkSamples");

        // Register a samples extension to configure published samples
        SamplesInternal extension = configureSamplesExtension(project, layout);

        // Samples
        // Generate wrapper files that can be shared by all samples
        FileTree wrapperFiles = createWrapperFiles(tasks, objects);
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, sample));

        // Samples binaries
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        FileCollection generatedSanityCheckTest = createGeneratedTests(tasks, objects, layout);
        extension.getBinaries().withType(SampleExemplarBinary.class).all(binary -> {
            if (!binary.getExplicitSanityCheck().get()) {
                binary.getTestsContent().from(generatedSanityCheckTest);
            }
        });

        extension.getBinaries().withType(SampleContentBinary.class).all(binary -> {
            createTasksForContentBinary(tasks, binary);
            createCheckTasksForContentBinary(tasks, binary, check);
            createTasksForSampleContentBinary(tasks, binary);
        });
        extension.getBinaries().withType(SampleInstallBinary.class).all(binary -> createTasksForSampleInstallBinary(tasks, binary));
        extension.getBinaries().withType(SampleArchiveBinary.class).all(binary -> createTasksForSampleArchiveBinary(tasks, layout, binary));

        // Documentation for sample index
        registerGenerateSampleIndex(tasks, providers, objects, extension);

        // Render all the documentation out to HTML
        TaskProvider<? extends Task> renderTask = renderSamplesDocumentation(tasks, assemble, check, extension);

        // Templates
        extension.getTemplates().configureEach(template -> applyConventionsForTemplates(extension, template));
        extension.getTemplates().all(template -> createTasksForTemplates(layout, tasks, template));

        // Testing (and binaries)
        extension.getBinaries().withType(SampleExemplarBinary.class).all(binary -> createTasksForSampleExemplarBinary(tasks, binary));
        configureExemplarTestsForSamples(project, layout, tasks, extension, check);
        createCheckTaskForAsciidoctorContentBinary(project, "checkAsciidoctorSampleContents", extension.getBinaries().withType(TestableAsciidoctorSampleContentBinary.class), check, asciidoctorConfiguration);

        // Trigger everything by realizing sample container
        project.afterEvaluate(p -> realizeSamples(extension, objects, assemble, check, wrapperFiles, project));

        tasks.register("samplesInformation", SamplesReport.class, task -> {
            task.getSamples().set(extension);
            task.setGroup("Documentation");
            task.setDescription("Generates listing for all available samples and templates.");
        });
    }

    private void createTasksForSampleExemplarBinary(TaskContainer tasks, SampleExemplarBinary binary) {
        TaskProvider<InstallSample> installSampleTaskForTesting = tasks.register("installSample" + capitalize(binary.getName()) + "ForTest", InstallSample.class, task -> {
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory with testing support.");
            task.getSource().from(binary.getTestsContent());
            task.getInstallDirectory().convention(binary.getTestedWorkingDirectory());
        });
        binary.getTestedInstallDirectory().convention(installSampleTaskForTesting.flatMap(InstallSample::getInstallDirectory));
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

    private SamplesInternal configureSamplesExtension(Project project, ProjectLayout layout) {
        SamplesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSamples();

        extension.getSamplesRoot().convention(layout.getProjectDirectory().dir("src/docs/samples"));

        // TODO: The following is only in samples
        extension.getInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/install"));
        extension.getDistribution().getInstalledSamples().from(extension.getInstallRoot());
        extension.getDistribution().getInstalledSamples().builtBy((Callable<List<DirectoryProperty>>) () -> extension.getBinaries().withType(SampleInstallBinary.class).stream().map(SampleInstallBinary::getInstallDirectory).collect(Collectors.toList()));
        extension.getDistribution().getZippedSamples().from((Callable<List<RegularFileProperty>>) () -> extension.getBinaries().withType(SampleArchiveBinary.class).stream().map(SampleArchiveBinary::getZipFile).collect(Collectors.toList()));

        // Testing
        // TODO: The following is only in samples
        extension.getTestedInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/testing"));
        extension.getDistribution().getTestedInstalledSamples().from(extension.getTestedInstallRoot());
        extension.getDistribution().getTestedInstalledSamples().builtBy(extension.getDistribution().getInstalledSamples().builtBy((Callable<List<DirectoryProperty>>) () -> extension.getBinaries().withType(SampleExemplarBinary.class).stream().map(SampleExemplarBinary::getTestedInstallDirectory).collect(Collectors.toList())));

        // Templates
        // TODO: The following is only in samples
        extension.getTemplatesRoot().convention(layout.getProjectDirectory().dir("src/docs/samples/templates"));

        extension.getDocumentationInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/docs/"));
        extension.getRenderedDocumentationRoot().convention(layout.getBuildDirectory().dir("working/samples/render-samples"));
        return extension;
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

    private void applyConventionsForSamples(SamplesInternal extension, SampleInternal sample) {
        String name = sample.getName();
        sample.getSampleDirectory().convention(extension.getSamplesRoot().dir(toKebabCase(name)));
        sample.getReadmeFile().convention(sample.getSampleDirectory().file("README.adoc"));
        sample.getPromoted().convention(Boolean.TRUE);
        sample.getDisplayName().convention(toTitleCase(name));
        sample.getDescription().convention("");
        sample.getCategory().convention("Uncategorized");
        sample.getSampleDocName().convention("sample_" + toSnakeCase(name));

        sample.getInstallDirectory().convention(extension.getInstallRoot().dir(toKebabCase(name)));
        sample.getDsls().convention(sample.getSampleDirectory().map(sampleDirectory -> {
            List<Dsl> dsls = new ArrayList<>();
            for (Dsl dsl : Dsl.values()) {
                if (sampleDirectory.dir(dsl.getConventionalDirectory()).getAsFile().exists()) {
                    dsls.add(dsl);
                }
            }
            return dsls;
        }));

        sample.getCommonContent().from(sample.getSampleDirectory().dir("common"));
        sample.getGroovyContent().from(sample.getSampleDirectory().dir(Dsl.GROOVY.getConventionalDirectory()));
        sample.getKotlinContent().from(sample.getSampleDirectory().dir(Dsl.KOTLIN.getConventionalDirectory()));

        // TODO: The guides should have the same thing
        sample.getAssembleTask().configure(task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample.");
        });
        sample.getCheckTask().configure(task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Checks '" + sample.getName() + "' sample.");
        });
    }

    private void registerGenerateSampleIndex(TaskContainer tasks, ProviderFactory providers, ObjectFactory objects, SamplesInternal extension) {
        TaskProvider<GenerateSampleIndexAsciidoc> generateSampleIndex = tasks.register("generateSampleIndex", GenerateSampleIndexAsciidoc.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Generate index page that contains all published samples.");
            task.getSamples().convention(providers.provider(() -> extension.getPublishedSamples().stream().map(sample -> toSummary(objects, sample)).collect(Collectors.toSet())));
            // TODO: This ignores changes to the temporary directory
            task.getOutputFile().set(new File(task.getTemporaryDir(), "index.adoc"));
        });
        extension.getSampleIndexFile().convention(generateSampleIndex.flatMap(GenerateSampleIndexAsciidoc::getOutputFile));
    }

    private TaskProvider<? extends Task> renderSamplesDocumentation(TaskContainer tasks, TaskProvider<Task> assemble, TaskProvider<Task> check, SamplesInternal extension) {
        TaskProvider<SyncWithProvider> assembleDocs = tasks.register("assembleSamples", SyncWithProvider.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Assembles all intermediate files needed to generate the samples documentation.");

            task.from(extension.getSampleIndexFile());
            task.from(extension.getDistribution().getZippedSamples(), sub -> sub.into("zips"));
            task.from(extension.getDistribution().getInstalledSamples(), sub -> sub.into("samples"));

            extension.getBinaries().withType(SampleContentBinary.class).all(binary -> {
                // TODO: Minor difference with guide (copy the file) maybe we should have a file collection from the root
                task.from(binary.getIndexPageFile());
            });
            task.into(extension.getDocumentationInstallRoot());
        });

        extension.getBinaries().withType(SampleContentBinary.class).configureEach(binary -> {
            binary.getInstalledIndexPageFile().fileProvider(assembleDocs.map(task -> new File(task.getDestinationDir(), binary.getSourcePermalink().get())));
        });

        TaskProvider<LockReleasingAsciidoctorTask> samplesMultiPage = tasks.register("samplesMultiPage", LockReleasingAsciidoctorTask.class, task -> {
            task.getInputs().files("samples").withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();

            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Generates multi-page samples index.");
            task.dependsOn(assembleDocs);
            Map<String, Object> attributes = new HashMap<>(genericAttributes());

            cleanStaleFiles(task); // TODO: Stale zip files
            configureResources(task, extension.getBinaries().withType(SampleContentBinary.class));
            configureSources(task, extension.getBinaries().withType(SampleContentBinary.class));
            task.sources(new Closure(null) {
                public Object doCall(Object ignore) {
                    ((PatternSet) this.getDelegate()).include("index.adoc");
                    return null;
                }
            });

            // TODO: This breaks the provider
            task.setSourceDir(extension.getDocumentationInstallRoot().get().getAsFile());
            // TODO: This breaks the provider
            task.setOutputDir(extension.getRenderedDocumentationRoot().get().getAsFile());

            task.setSeparateOutputDirs(false);

            // TODO: Figure out why so much difference with guides
            // TODO: This is specific to gradle/gradle
            attributes.put("userManualPath", "../userguide");
            task.attributes(attributes);
        });
        extension.getDistribution().getRenderedDocumentation().from(samplesMultiPage);

        assemble.configure(t -> t.dependsOn(extension.getDistribution().getRenderedDocumentation()));

        extension.getBinaries().withType(SampleContentBinary.class).configureEach(binary -> {
            binary.getRenderedPageFile().fileProvider(samplesMultiPage.map(it -> new File(it.getOutputDir(), binary.getRenderedPermalink().get())));
            binary.getViewablePageFile().fileProvider(samplesMultiPage.map(it -> new File(it.getOutputDir(), binary.getRenderedPermalink().get())));
        });

        return samplesMultiPage;
    }

    private void configureExemplarTestsForSamples(Project project, ProjectLayout layout, TaskContainer tasks, SamplesInternal extension, TaskProvider<Task> check) {
        SourceSet sourceSet = project.getExtensions().getByType(SourceSetContainer.class).getByName(DOCS_TEST_SOURCE_SET_NAME);
        tasks.register("generateSamplesExemplarFunctionalTest", GenerateTestSource.class, task -> {
            task.setDescription("Generates source file to run exemplar tests for published samples.");
            task.setGroup("Build Init");
            task.getOutputDirectory().convention(layout.getProjectDirectory().dir("src/" + DOCS_TEST_SOURCE_SET_NAME + "/java"));
        });

        DependencyHandler dependencies = project.getDependencies();
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.gradle:sample-check:0.12.6");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "org.slf4j:slf4j-simple:1.7.16");
        dependencies.add(sourceSet.getImplementationConfigurationName(), "junit:junit:4.12");

        TaskProvider<Test> exemplarTest = tasks.named(sourceSet.getName(), Test.class);
        exemplarTest.configure(task -> {
            DirectoryProperty samplesDirectory = extension.getTestedInstallRoot();
            task.getInputs().dir(samplesDirectory).withPathSensitivity(PathSensitivity.RELATIVE);
            // TODO: This isn't lazy.  Need a different API here.
            task.systemProperty("integTest.samplesdir", samplesDirectory.get().getAsFile().getAbsolutePath());
            task.dependsOn(extension.getDistribution().getTestedInstalledSamples());
        });

        // TODO: Make the sample's check task depend on this test?
        check.configure(task -> task.dependsOn(exemplarTest));
    }

    private void createTasksForSampleInstallBinary(TaskContainer tasks, SampleInstallBinary binary) {
        TaskProvider<InstallSample> installSampleTask = tasks.register("installSample" + capitalize(binary.getName()), InstallSample.class, task -> {
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory.");
            task.getSource().from(binary.getContent());
            task.getExcludes().convention(binary.getExcludes());
            task.getReadmeName().convention(binary.getSampleLinkName().map(name -> name + ".adoc"));
            task.getInstallDirectory().convention(binary.getWorkingDirectory());
        });
        binary.getInstallDirectory().convention(installSampleTask.flatMap(InstallSample::getInstallDirectory));
    }

    private void createTasksForSampleArchiveBinary(TaskContainer tasks, ProjectLayout layout, SampleArchiveBinary binary) {
        TaskProvider<ValidateSampleBinary> validateSample = tasks.register("validateSample" + capitalize(binary.getName()), ValidateSampleBinary.class, task -> {
            task.setDescription("Checks the sample '" + binary.getName() + "' is valid.");
            task.getDsl().convention(binary.getDsl());
            task.getSampleName().convention(binary.getName());
            task.getZipFile().convention(binary.getZipFile());
            task.getReportFile().convention(layout.getBuildDirectory().file("reports/sample-validation/" + task.getName() + ".txt"));
        });
        binary.getValidationReport().convention(validateSample.flatMap(ValidateSampleBinary::getReportFile));

        TaskProvider<ZipSample> zipTask = tasks.register("zipSample" + capitalize(binary.getName()), ZipSample.class, task -> {
            task.setDescription("Creates a zip for sample '" + binary.getName() + "'.");
            task.getSource().from(binary.getInstallDirectory());
            task.getMainSource().from(binary.getDslSpecificContent());
            task.getExcludes().convention(binary.getExcludes());
            // TODO: make this relocatable too?
            task.getArchiveFile().convention(layout.getBuildDirectory().file(binary.getSampleLinkName().map(name -> String.format("sample-zips/%s-%s.zip", name, binary.getDsl().get().getDslLabel()))));
        });
        binary.getZipFile().convention(zipTask.flatMap(ZipSample::getArchiveFile));
    }

    private void realizeSamples(SamplesInternal extension, ObjectFactory objects, TaskProvider<Task> assemble, TaskProvider<Task> check, FileTree wrapperFiles, Project project) {
        // TODO: Project is passed strictly for zipTree method
        // TODO: Disallow changes to published samples container after this point.
        for (SampleInternal sample : extension.getPublishedSamples()) {
            assertNameDoesNotContainsDisallowedCharacters(sample, "Sample '%s' has disallowed characters", sample.getName());

            sample.getDsls().disallowChanges();
            // TODO: This should only be enforced if we are trying to build the given sample
            Set<Dsl> dsls = sample.getDsls().get();
            if (dsls.isEmpty()) {
                throw new GradleException("Samples must define at least one DSL, sample '" + sample.getName() + "' has none.");
            }

            sample.getPromoted().disallowChanges();

            if (sample.getPromoted().get()) {
                // Promoted samples have a readme and an entry in the sample index
                SampleContentBinary contentBinary = objects.newInstance(SampleContentBinary.class, sample.getName());
                extension.getBinaries().add(contentBinary);
                contentBinary.getDisplayName().convention(sample.getDisplayName());
                contentBinary.getSampleDirectory().convention(sample.getSampleDirectory());
                contentBinary.getBaseName().convention(sample.getSampleDocName());
                contentBinary.getSummary().convention(toSummary(objects, sample));
                contentBinary.getRenderedPermalink().convention(contentBinary.getBaseName().map(baseName -> baseName + ".html"));
                contentBinary.getSourcePermalink().convention(contentBinary.getBaseName().map(baseName -> baseName + ".adoc"));
                contentBinary.getResourceFiles().from(extension.getDistribution().getZippedSamples());
                contentBinary.getResourceSpec().convention(project.copySpec(spec -> spec.from(extension.getDistribution().getZippedSamples(), sub -> sub.into("zips"))));
                contentBinary.getSourcePattern().convention(contentBinary.getBaseName().map(baseName -> baseName + ".adoc"));
                contentBinary.getSampleInstallDirectory().convention(sample.getInstallDirectory());
                contentBinary.getSourcePageFile().convention(sample.getReadmeFile());
                contentBinary.getGradleVersion().convention(project.getGradle().getGradleVersion());

                for (Dsl dsl : dsls) {
                    // Each promoted binary is put into a zip that can be downloaded later
                    SampleArchiveBinary binary = registerSampleArchiveBinaryForDsl(extension, sample, dsl, objects, wrapperFiles, contentBinary);
                    sample.getAssembleTask().configure(task -> task.dependsOn(binary.getZipFile()));
                    sample.getCheckTask().configure(task -> task.dependsOn(binary.getValidationReport()));

                    // Each README is tested
                    TestableAsciidoctorSampleContentBinary testableContentBinary = objects.newInstance(TestableAsciidoctorSampleContentBinary.class, sample.getName() + dsl.getDisplayName());
                    testableContentBinary.getContentFile().convention(contentBinary.getInstalledIndexPageFile());
                    testableContentBinary.getStartingSampleDirectory().convention(binary.getInstallDirectory());
                    extension.getBinaries().add(testableContentBinary);

                    SampleExemplarBinary exemplarBinary = registerExemplarBinaryForTestedBinary(extension, objects, sample, dsl);
                    exemplarBinary.getTestsContent().from(binary.getInstallDirectory());
                }
            } else {
                // Unpromoted samples are just tested
                for (Dsl dsl : dsls) {
                    SampleInstallBinary binary = registerSampleInstallBinaryForDsl(extension, sample, dsl, objects, wrapperFiles);
                    sample.getAssembleTask().configure(task -> task.dependsOn(binary.getInstallDirectory()));

                    SampleExemplarBinary exemplarBinary = registerExemplarBinaryForTestedBinary(extension, objects, sample, dsl);
                    exemplarBinary.getTestsContent().from(binary.getInstallDirectory());
                }
            }

            // TODO: To make this lazy without afterEvaluate/eagerness, we need to be able to tell the tasks container that the samples container should be consulted
            assemble.configure(t -> t.dependsOn(sample.getAssembleTask()));
            check.configure(t -> t.dependsOn(sample.getCheckTask()));
        }
    }

    private SampleArchiveBinary registerSampleArchiveBinaryForDsl(SamplesInternal extension, SampleInternal sample, Dsl dsl, ObjectFactory objects, FileTree wrapperFiles, SampleContentBinary contentBinary) {
        SampleArchiveBinary binary = objects.newInstance(SampleArchiveBinary.class, sample.getName() + dsl.getDisplayName());
        binary.getDsl().convention(dsl).disallowChanges();
        binary.getSampleLinkName().convention(sample.getSampleDocName()).disallowChanges();
        binary.getWorkingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
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
        binary.getExcludes().convention(Arrays.asList("**/build/**", "**/.gradle/**"));
        binary.getContent().from(wrapperFiles);
        binary.getContent().from(contentBinary.getIndexPageFile());
        binary.getContent().from(sample.getCommonContent());
        binary.getContent().from(binary.getDslSpecificContent());
        binary.getContent().disallowChanges();

        extension.getBinaries().add(binary);
        return binary;
    }

    private SampleInstallBinary registerSampleInstallBinaryForDsl(SamplesInternal extension, SampleInternal sample, Dsl dsl, ObjectFactory objects, FileTree wrapperFiles) {
        SampleInstallBinary binary = objects.newInstance(SampleInstallBinary.class, sample.getName() + dsl.getDisplayName());
        binary.getWorkingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
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
        binary.getExcludes().convention(Arrays.asList("**/build/**", "**/.gradle/**"));
        binary.getContent().from(wrapperFiles);
        binary.getContent().from(sample.getCommonContent());
        binary.getContent().from(binary.getDslSpecificContent());
        binary.getContent().disallowChanges();

        extension.getBinaries().add(binary);
        return binary;
    }

    private SampleExemplarBinary registerExemplarBinaryForTestedBinary(SamplesInternal extension, ObjectFactory objects, SampleInternal sample, Dsl dsl) {
        SampleExemplarBinary exemplarBinary = objects.newInstance(SampleExemplarBinary.class, sample.getName() + dsl.getDisplayName());
        exemplarBinary.getTestedWorkingDirectory().convention(extension.getTestedInstallRoot().dir(toKebabCase(sample.getName()) + "/" + dsl.getConventionalDirectory())).disallowChanges();

        exemplarBinary.getTestsContent().from(sample.getSampleDirectory().dir("tests")); // TODO (donat) maybe deprecate with warning
        exemplarBinary.getTestsContent().from(sample.getSampleDirectory().dir("tests-common"));
        exemplarBinary.getTestsContent().from(sample.getSampleDirectory().dir("tests-" + dsl.getDisplayName().toLowerCase()));
        exemplarBinary.getTestsContent().from(sample.getTestsContent());

        boolean hasSanityCheck = sample.getSampleDirectory().dir("tests").get().getAsFileTree().getFiles().stream().anyMatch(f -> f.getName().endsWith(".sample.conf") && f.getName().toLowerCase().contains("sanitycheck"));
        hasSanityCheck |= sample.getSampleDirectory().dir("tests-common").get().getAsFileTree().getFiles().stream().anyMatch(f -> f.getName().endsWith(".sample.conf") && f.getName().toLowerCase().contains("sanitycheck"));
        exemplarBinary.getExplicitSanityCheck().value(hasSanityCheck);
        extension.getBinaries().add(exemplarBinary);
        return exemplarBinary;
    }

    // Public only while we migrate the code from SamplesPlugin
    public static SampleSummary toSummary(ObjectFactory objects, SampleInternal sample) {
        SampleSummary summary = objects.newInstance(SampleSummary.class);
        summary.getDisplayName().set(sample.getDisplayName());
        summary.getDsls().set(sample.getDsls());
        summary.getCategory().set(sample.getCategory());
        summary.getDescription().set(sample.getDescription());
        summary.getSampleDocName().set(sample.getSampleDocName());
        summary.getPromoted().set(sample.getPromoted());
        return summary;
    }
}
