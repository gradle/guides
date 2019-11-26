package org.gradle.samples.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.samples.Dsl;
import org.gradle.samples.Sample;
import org.gradle.samples.SampleBinary;
import org.gradle.samples.SamplesExtension;
import org.gradle.samples.internal.tasks.GenerateSampleIndexAsciidoc;
import org.gradle.samples.internal.tasks.GenerateSamplePageAsciidoc;
import org.gradle.samples.internal.tasks.InstallSample;
import org.gradle.samples.internal.tasks.ValidateSampleBinary;
import org.gradle.samples.internal.tasks.ZipSample;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.samples.internal.StringUtils.toTitleCase;

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

        project.getPluginManager().apply("base");

        // Register a samples extension to configure published samples
        SamplesExtension extension = createSamplesExtension(project, layout);

        // Generate wrapper files that can be shared by all samples
        FileTree wrapperFiles = createWrapperFiles(tasks, objectFactory);
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, wrapperFiles, sample));

        // Create tasks for each sample binary (a sample for a particular DSL)
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().all(binary -> createTasksForSampleBinary(tasks, layout, binary));

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME);

        registerGenerateSampleIndex(tasks, extension);
        TaskProvider<Sync> assembleDocs = tasks.register("assembleSampleDocs", Sync.class, task -> {
            task.from(extension.getSampleIndexFile());
            task.from((Callable<List<RegularFileProperty>>) () -> extension.getPublishedSamples().stream().map(Sample::getSamplePageFile).collect(Collectors.toList()));
            task.from((Callable<List<RegularFileProperty>>) () -> extension.getBinaries().stream().map(SampleBinary::getZipFile).collect(Collectors.toList()), copySpec -> {
                copySpec.into("zips");
            });
            task.into(extension.getDocumentationRoot());
        });
        extension.getAssembledDocumentation().from(assembleDocs);

        assemble.configure(t -> t.dependsOn(extension.getAssembledDocumentation()));

        project.afterEvaluate(p -> {
            // TODO: Disallow changes to published samples container after this point.
            for (Sample sample : extension.getPublishedSamples()) {

                // TODO: Make these lifecycle tasks a part of the sample component?
                TaskProvider<Task> assembleSample = registerAssembleForSample(tasks, sample);
                assemble.configure(t -> t.dependsOn(assembleSample));

                TaskProvider<Task> checkSample = registerCheckForSample(tasks, sample);
                check.configure(t -> t.dependsOn(checkSample));

                registerGenerateSamplePage(tasks, sample);

                sample.getDsls().disallowChanges();
                // TODO: This should only be enforced if we are trying to build the given sample
                List<Dsl> dsls = sample.getDsls().get();
                if (dsls.isEmpty()) {
                    throw new GradleException("Samples must have at least one DSL, sample '" + sample.getName() + "' has none.");
                }
                for (Dsl dsl : dsls) {
                    Provider<SampleBinary> dslBinary = registerSampleBinaryForDsl(extension, sample, dsl);
                    assembleSample.configure(task -> {
                        task.dependsOn(dslBinary.flatMap(SampleBinary::getZipFile));
                        task.dependsOn(dslBinary.flatMap(SampleBinary::getInstallDirectory));
                    });
                    // TODO: Wire validate tasks into the sample check task
                }
            }
        });

        // TODO: Re-enable this
//        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
//        project.getConfigurations().maybeCreate("asciidoctor");
//        project.getDependencies().add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0");
        // project.getPluginManager().apply(TestingSamplesWithExemplarPlugin.class);
    }

    private SamplesExtension createSamplesExtension(Project project, ProjectLayout layout) {
        SamplesExtension extension = project.getExtensions().create(SamplesExtension.class, "samples", DefaultSamplesExtension.class);
        extension.getSamplesRoot().convention(layout.getProjectDirectory().dir("src/samples"));
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

    private TaskProvider<Task> registerAssembleForSample(TaskContainer tasks, Sample sample) {
        return tasks.register("assemble" + StringUtils.capitalize(sample.getName() + "Sample"), task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Assembles '" + sample.getName() + "' sample.");
        });
    }

    private TaskProvider<Task> registerCheckForSample(TaskContainer tasks, Sample sample) {
        return tasks.register("check" + StringUtils.capitalize(sample.getName() + "Sample"), task -> {
            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            task.setDescription("Checks '" + sample.getName() + "' sample.");
        });
    }

    private void registerGenerateSamplePage(TaskContainer tasks, Sample sample) {
        TaskProvider<GenerateSamplePageAsciidoc> generateSamplePage = tasks.register("generate" + StringUtils.capitalize(sample.getName()) + "Page", GenerateSamplePageAsciidoc.class, task -> {
            task.getSourceFile().convention(sample.getReadMeFile());
            // TODO: This ignores changes to the temporary directory
            task.getOutputFile().set(new File(task.getTemporaryDir(), "sample_" + sample.getName() + ".adoc"));
            task.setDescription("Generates asciidoc page for sample '" + sample.getName() + "'");
        });
        sample.getSamplePageFile().convention(generateSamplePage.flatMap(GenerateSamplePageAsciidoc::getOutputFile));
    }

    private void applyConventionsForSamples(SamplesExtension extension, FileTree wrapperFiles, Sample sample) {
        String name = sample.getName();
        sample.getDisplayName().convention(toTitleCase(name));
        sample.getSampleDirectory().convention(extension.getSamplesRoot().dir(name));
        sample.getInstallDirectory().convention(extension.getInstallRoot().dir(name));
        sample.getDescription().convention("");

        sample.getLicenseFile().convention(sample.getSampleDirectory().file("LICENSE"));
        sample.getReadMeFile().convention(sample.getSampleDirectory().file("README.adoc"));

        sample.getDsls().convention(Arrays.asList(Dsl.values()));
        sample.common(content -> {
            content.from(sample.getLicenseFile());
            content.from(sample.getSamplePageFile());
            content.from(wrapperFiles);
        });
        sample.groovy(content -> content.from(sample.getSampleDirectory().dir(Dsl.GROOVY.getConventionalDirectory())));
        sample.kotlin(content -> content.from(sample.getSampleDirectory().dir(Dsl.KOTLIN.getConventionalDirectory())));
    }

    private Provider<SampleBinary> registerSampleBinaryForDsl(SamplesExtension extension, Sample sample, Dsl dsl) {
        return extension.getBinaries().register(sample.getName() + dsl.getDisplayName(), binary -> {
            binary.getDsl().convention(dsl).disallowChanges();
            binary.getSamplePageFile().convention(sample.getSamplePageFile()).disallowChanges();
            switch (dsl) {
                case GROOVY:
                    binary.getStagingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
                    binary.getDslSpecificContent().from(sample.getGroovyContent()).disallowChanges();
                    break;
                case KOTLIN:
                    binary.getStagingDirectory().convention(sample.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
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
        TaskProvider<ValidateSampleBinary> validateSample = tasks.register("validateSample" + StringUtils.capitalize(binary.getName()), ValidateSampleBinary.class, task -> {
            task.getSampleBinary().convention(binary);
            task.getReportFile().convention(layout.getBuildDirectory().file("reports/sample-validation/" + task.getName() + ".txt"));
            task.setDescription("Checks the sample '" + binary.getName() + "' is valid.");
        });

        // TODO: Wire this into the sample instead?
        tasks.named("check").configure(task -> task.dependsOn(validateSample));

        TaskProvider<InstallSample> installSampleTask = tasks.register("installSample" + StringUtils.capitalize(binary.getName()), InstallSample.class, task -> {
            task.getSource().from(binary.getContent());
            task.getMainSource().from(binary.getDslSpecificContent());
            task.getInstallDirectory().convention(binary.getStagingDirectory());
            task.getExcludes().convention(binary.getExcludes());
            task.setDescription("Installs sample '" + binary.getName() + "' into a local directory.");
        });
        binary.getInstallDirectory().convention(installSampleTask.flatMap(InstallSample::getInstallDirectory));

        TaskProvider<ZipSample> zipTask = tasks.register("zipSample" + StringUtils.capitalize(binary.getName()), ZipSample.class, task -> {
            task.getSource().from(binary.getContent());
            task.getMainSource().from(binary.getDslSpecificContent());
            task.getArchiveFile().convention(layout.getBuildDirectory().file("sample-zips/" + binary.getName() + ".zip"));
            task.getExcludes().convention(binary.getExcludes());
            task.setDescription("Creates a zip for sample '" + binary.getName() + "'.");
        });
        binary.getZipFile().convention(zipTask.flatMap(ZipSample::getArchiveFile));
    }
}
