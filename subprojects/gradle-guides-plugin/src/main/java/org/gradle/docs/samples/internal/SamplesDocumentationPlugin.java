package org.gradle.docs.samples.internal;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.internal.tasks.CheckLinks;
import org.gradle.docs.internal.tasks.ViewDocumentation;
import org.gradle.docs.samples.SampleSummary;
import org.gradle.docs.samples.internal.tasks.GenerateSamplePageAsciidoc;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.StringUtils.*;

public class SamplesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ProviderFactory providers = project.getProviders();
        ObjectFactory objects = project.getObjects();

        project.getPluginManager().apply(DocumentationBasePlugin.class);
        project.getPluginManager().apply("org.asciidoctor.convert"); // For the `asciidoctor` configuration

        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getDependencies().add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0");

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.register("checkSamples");

        // Register a samples extension to configure published samples
        SamplesInternal extension = configureSamplesExtension(project, layout);

        // Samples
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, sample));

        // Samples binaries
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().withType(SampleContentBinary.class).all(binary -> createTasksForSampleContentBinary(tasks, layout, providers, binary));

        // Render all the documentation out to HTML
        TaskProvider<? extends Task> renderTask = renderSamplesDocumentation(tasks, assemble, check, extension);

        // Trigger everything by realizing sample container
        project.afterEvaluate(p -> realizeSamples(extension, objects));
    }

    private SamplesInternal configureSamplesExtension(Project project, ProjectLayout layout) {
        SamplesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSamples();

        extension.getSamplesRoot().convention(layout.getProjectDirectory().dir("src/docs/samples"));

        extension.getDocumentationInstallRoot().convention(layout.getBuildDirectory().dir("working/samples/docs/"));
        extension.getRenderedDocumentationRoot().convention(layout.getBuildDirectory().dir("working/samples/render-samples"));
        return extension;
    }

    private void applyConventionsForSamples(SamplesInternal extension, SampleInternal sample) {
        String name = sample.getName();
        sample.getSampleDirectory().convention(extension.getSamplesRoot().dir(toKebabCase(name)));
        sample.getDisplayName().convention(toTitleCase(name));
        sample.getDescription().convention("");
        sample.getCategory().convention("Uncategorized");
    }

    private void createTasksForSampleContentBinary(TaskContainer tasks, ProjectLayout layout, ProviderFactory providers, SampleContentBinary binary) {
        TaskProvider<GenerateSamplePageAsciidoc> generateSamplePage = tasks.register("generate" + capitalize(binary.getName()) + "Page", GenerateSamplePageAsciidoc.class, task -> {
            task.setDescription("Generates asciidoc page for sample '" + binary.getName() + "'");

            task.getSampleSummary().convention(binary.getSummary());
            task.getReadmeFile().convention(binary.getSampleDirectory().file("README.adoc"));
            // TODO: Here the permalink is used as a baseName
            task.getOutputFile().fileProvider(binary.getPermalink().map(fileName -> new File(task.getTemporaryDir(), fileName + ".adoc")));
        });
        binary.getIndexPageFile().convention(generateSamplePage.flatMap(GenerateSamplePageAsciidoc::getOutputFile));
    }

    private TaskProvider<? extends Task> renderSamplesDocumentation(TaskContainer tasks, TaskProvider<Task> assemble, TaskProvider<Task> check, SamplesInternal extension) {
        TaskProvider<Sync> assembleDocs = tasks.register("assembleSamples", Sync.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Assembles all intermediate files needed to generate the samples documentation.");

            task.from(extension.getSampleIndexFile());
            task.from(extension.getDistribution().getZippedSamples(), sub -> sub.into("zips"));
            task.from(extension.getDistribution().getInstalledSamples(), sub -> sub.into("samples"));

            extension.getBinaries().withType(SampleContentBinary.class).forEach(binary -> {
                task.from(binary.getIndexPageFile(), sub -> sub.into(binary.getPermalink()));
            });
            task.into(extension.getDocumentationInstallRoot());
        });

        TaskProvider<AsciidoctorTask> samplesMultiPage = tasks.register("samplesMultiPage", AsciidoctorTask.class, task -> {
            task.getInputs().files("samples").withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();

            task.setGroup("documentation");
            task.setDescription("Generates multi-page samples index.");
            task.dependsOn(assembleDocs);

            task.sources(new Closure(null) {
                public Object doCall(Object ignore) {
                    // TODO: If we model baseName we could include each file one at a time from the binary (for both guide and sample)
                    ((PatternSet)this.getDelegate()).include("**/*.adoc");

                    // QUESTION: Why this exclude
                    ((PatternSet)this.getDelegate()).exclude("samples/**/*.adoc");
                    return null;
                }
            });

            // TODO: This breaks the provider
            task.setSourceDir(extension.getDocumentationInstallRoot().get().getAsFile());
            // TODO: This breaks the provider
            task.setOutputDir(extension.getRenderedDocumentationRoot().get().getAsFile());

            // TODO: Only used by samples
            task.resources(new Closure(task) {
                public Object doCall(Object ignore) {
                    ((CopySpec)this.getDelegate()).from(extension.getDistribution().getZippedSamples(), sub -> sub.into("zips"));
                    return this.getDelegate();
                }
            });

            task.setSeparateOutputDirs(false);

            // TODO: Figure out why so much difference with guides
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

            // Fail on rendering errors
            List<String> capturedOutput = new ArrayList<>();
            StandardOutputListener listener = it -> capturedOutput.add(it.toString());

            task.getLogging().addStandardErrorListener(listener);
            task.getLogging().addStandardOutputListener(listener);

            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task t) {
                    task.getLogging().removeStandardOutputListener(listener);
                    task.getLogging().removeStandardErrorListener(listener);
                    String output = capturedOutput.stream().collect(Collectors.joining());
                    if (output.indexOf("include file not found:") > 0) {
                        throw new RuntimeException("Include file(s) not found.");
                    }
                }
            });
        });
        extension.getDistribution().getRenderedDocumentation().from(samplesMultiPage);

        assemble.configure(t -> t.dependsOn(extension.getDistribution().getRenderedDocumentation()));

        extension.getBinaries().withType(SampleContentBinary.class).configureEach(binary -> {
            tasks.register("view" + capitalize(binary.getName()) + "Sample", ViewDocumentation.class, task -> {
                task.setGroup("Documentation");
                task.setDescription("Generates the guide and open in the browser");
                // TODO: Permalink used as baseName
                task.getIndexFile().fileProvider(samplesMultiPage.map(it -> new File(it.getOutputDir(), binary.getPermalink().get() + ".html")));
            });

            TaskProvider<CheckLinks> checkLinksTask = tasks.register("check" + capitalize(binary.getName()) + "Links", CheckLinks.class, task -> {
                // TODO: Permalink used as baseName
                task.getIndexDocument().fileProvider(samplesMultiPage.map(it -> new File(it.getOutputDir(), binary.getPermalink().get() + ".html")));
            });

            check.configure(it -> it.dependsOn(checkLinksTask));
        });

        return samplesMultiPage;
    }

    private void realizeSamples(SamplesInternal extension, ObjectFactory objects) {
        // TODO: Disallow changes to published samples container after this point.
        for (SampleInternal sample : extension.getPublishedSamples()) {
            if (sample.getName().contains("_") || sample.getName().contains("-")) {
                throw new IllegalArgumentException(String.format("Sample '%s' has disallowed characters", sample.getName()));
            }

            SampleContentBinary binary = objects.newInstance(SampleContentBinary.class, sample.getName());
            extension.getBinaries().add(binary);
            binary.getDisplayName().convention(sample.getDisplayName());
            binary.getSampleDirectory().convention(sample.getSampleDirectory());
            binary.getPermalink().convention(sample.getSampleDocName());
            binary.getSummary().convention(toSummary(objects, sample));

            // For compatibility
            sample.getSamplePageFile().convention(binary.getIndexPageFile());
        }
    }

    // Public only while we migrate the code from SamplesPlugin
    public static SampleSummary toSummary(ObjectFactory objects, SampleInternal sample) {
        SampleSummary summary = objects.newInstance(SampleSummary.class);
        summary.getDisplayName().set(sample.getDisplayName());
        summary.getDsls().set(sample.getDsls());
        summary.getCategory().set(sample.getCategory());
        summary.getDescription().set(sample.getDescription());
        summary.getSampleDocName().set(sample.getSampleDocName());
        return summary;
    }
}
