package org.gradle.docs.guides.internal;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.docs.guides.internal.tasks.GenerateGuidePageAsciidoc;
import org.gradle.docs.guides.internal.tasks.ViewGuide;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.FileUtils.deleteDirectory;
import static org.gradle.docs.internal.StringUtils.*;

public class GuidesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ProviderFactory providers = project.getProviders();
        ObjectFactory objects = project.getObjects();
        Gradle gradle = project.getGradle();

        project.getPluginManager().apply(DocumentationBasePlugin.class);
        project.getPluginManager().apply("org.asciidoctor.convert"); // For the `asciidoctor` configuration

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);

        // Configure the guides extension to configure published samples
        GuidesInternal extension = configureGuidesExtension(project, layout);

        // Guides
        extension.getPublishedGuides().configureEach(guide -> applyConventionsForGuides(extension, gradle, guide));

        // Guide binaries
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().all(binary -> createTasksForGuideBinary(tasks, layout, providers, binary));

        // Render all the documentation out to HTML
        TaskProvider<? extends Task> renderTask = renderGuidesDocumentation(tasks, assemble, extension);

        // Publish the guides to consumers
        createPublishGuidesElements(project.getConfigurations(), objects, renderTask, extension);

        // Trigger everything by realizing guide container
        project.afterEvaluate(p -> realizeGuides(extension));
    }

    private Configuration createPublishGuidesElements(ConfigurationContainer configurations, ObjectFactory objects, TaskProvider<? extends Task> renderTask, GuidesInternal extension) {
        return configurations.create("guideDocsElements", configuration -> {
            configuration.setVisible(true);
            configuration.setCanBeResolved(false);
            configuration.setCanBeConsumed(true);
            configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "docs"));
            configuration.getAttributes().attribute(Attribute.of("type", String.class), "guide-docs");
            configuration.getOutgoing().artifact(extension.getRenderedDocumentationRoot(), it -> it.builtBy(renderTask));
        });
    }

    private GuidesInternal configureGuidesExtension(Project project, ProjectLayout layout) {
        GuidesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getGuides();

        extension.getGuidesRoot().convention(layout.getProjectDirectory().dir("src/docs/guides"));

        extension.getDocumentationInstallRoot().convention(layout.getBuildDirectory().dir("working/guides/docs/"));
        extension.getRenderedDocumentationRoot().convention(layout.getBuildDirectory().dir("working/guides/render-guides"));
        return extension;
    }

    private void applyConventionsForGuides(GuidesInternal extension, Gradle gradle, GuideInternal guide) {
        String name = guide.getName();
        guide.getGuideDirectory().convention(extension.getGuidesRoot().dir(toKebabCase(name)));
        guide.getRepositoryPath().convention("gradle-guides/" + toKebabCase(name));
        guide.getMinimumGradleVersion().convention(gradle.getGradleVersion());
        guide.getTitle().convention(toTitleCase(name));
        guide.getDescription().convention("");
        guide.getCategory().convention("Uncategorized");
        guide.getPermalink().convention(toSnakeCase(guide.getName()));
    }

    private void createTasksForGuideBinary(TaskContainer tasks, ProjectLayout layout, ProviderFactory providers, GuideBinary binary) {
        TaskProvider<GenerateGuidePageAsciidoc> generateGuidePage = tasks.register("generate" + capitalize(binary.getName()) + "Page", GenerateGuidePageAsciidoc.class, task -> {
            task.setDescription("Generates asciidoc page for sample '" + binary.getName() + "'");

            // TODO: Allow multiple guide per project
            task.getAttributes().empty();
            task.getAttributes().put("projdir", layout.getProjectDirectory().getAsFile().getAbsolutePath());
            task.getAttributes().put("codedir", layout.getProjectDirectory().file("src/main").getAsFile().getAbsolutePath());
            task.getAttributes().put("testdir", layout.getProjectDirectory().file("src/test").getAsFile().getAbsolutePath());
            task.getAttributes().put("samplescodedir", layout.getProjectDirectory().file("samples/code").getAsFile().getAbsolutePath());
            task.getAttributes().put("samplesoutputdir", layout.getProjectDirectory().file("samples/output").getAsFile().getAbsolutePath());
            task.getAttributes().put("samples-dir", layout.getProjectDirectory().file("samples").getAsFile().getAbsolutePath());
            task.getAttributes().put("gradle-version", binary.getGradleVersion());
            task.getAttributes().put("user-manual", binary.getGradleVersion().map(v -> "https://docs.gradle.org/" + v + "/userguide/"));
            task.getAttributes().put("language-reference", binary.getGradleVersion().map(v -> "https://docs.gradle.org/" + v + "/dsl/"));
            task.getAttributes().put("api-reference", binary.getGradleVersion().map(v -> "https://docs.gradle.org/" + v + "/javadoc/"));
            task.getAttributes().put("repository-path", binary.getRepositoryPath());
            task.getAttributes().put("guide-title", binary.getDisplayName());
            task.getIndexFile().convention(binary.getGuideDirectory().file("contents/index.adoc"));
            task.getOutputFile().fileProvider(providers.provider(() -> new File(task.getTemporaryDir(), "index.adoc")));
        });
        binary.getIndexPageFile().convention(generateGuidePage.flatMap(GenerateGuidePageAsciidoc::getOutputFile));
    }

    private TaskProvider<? extends Task> renderGuidesDocumentation(TaskContainer tasks, TaskProvider<Task> assemble, GuidesInternal extension) {
        TaskProvider<Sync> assembleDocs = tasks.register("assembleGuides", Sync.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Assembles all intermediate files needed to generate the samples documentation.");

            extension.getBinaries().forEach(binary -> {
                task.from(binary.getGuideDirectory().dir("contents"), sub -> {
                    sub.into(binary.getPermalink());
                    sub.include("**/*.adoc");
                    sub.include("**/*.txt");
                });
                task.from(binary.getIndexPageFile(), sub -> sub.into(binary.getPermalink()));
            });
            task.into(extension.getDocumentationInstallRoot());
        });

        TaskProvider<AsciidoctorTask> guidesMultiPage = tasks.register("guidesMultiPage", AsciidoctorTask.class, task -> {
            task.getInputs().files("samples").withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();

            task.setGroup("documentation");
            task.setDescription("Generates multi-page samples index.");
            task.dependsOn(assembleDocs);

            task.sources(new Closure(null) {
                public Object doCall(Object ignore) {
                    // Render only the index.adoc file
                    ((PatternSet)this.getDelegate()).include("**/index.adoc");
                    return null;
                }
            });

            task.doFirst(t -> {
                deleteDirectory(extension.getRenderedDocumentationRoot().get().getAsFile());
            });

            task.doLast(t -> {
                task.getProject().copy(spec -> {
                    extension.getBinaries().forEach(binary -> {
                        spec.from(binary.getGuideDirectory().dir("contents/images"), sub -> {
                            sub.into(binary.getPermalink().get() + "/images");
                        });
                    });
                    spec.into(extension.getRenderedDocumentationRoot());
                });
            });

            // TODO: This breaks the provider
            task.setSourceDir(extension.getDocumentationInstallRoot().get().getAsFile());
            // TODO: This breaks the provider
            task.setOutputDir(extension.getRenderedDocumentationRoot().get().getAsFile());

            task.setSeparateOutputDirs(false);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("doctype", "book");
            attributes.put("icons", "font");
            attributes.put("source-highlighter", "prettify");
            attributes.put("toc", "auto");
            attributes.put("toclevels", 1);
            attributes.put("toc-title", "Contents");
            // TODO: This is specific to guides
            attributes.put("imagesdir", "images");
            attributes.put("stylesheet", null);
            attributes.put("linkcss", true);
            attributes.put("docinfodir", ".");
            attributes.put("docinfo1", "");
            attributes.put("nofooter", true);
            attributes.put("sectanchors", true);
            attributes.put("sectlinks", true);
            attributes.put("linkattrs", true);
            attributes.put("encoding", "utf-8");
            attributes.put("idprefix", "");
            attributes.put("guides", "https://guides.gradle.org");
            attributes.put("user-manual-name", "User Manual");
            task.attributes(attributes);

            // Fail on rendering errors
            List<String> capturedOutput = new ArrayList<>();
            StandardOutputListener listener = it -> capturedOutput.add(it.toString());

            task.getLogging().addStandardErrorListener(listener);
            task.getLogging().addStandardOutputListener(listener);

            task.doLast(t -> {
                task.getLogging().removeStandardOutputListener(listener);
                task.getLogging().removeStandardErrorListener(listener);
                String output = capturedOutput.stream().collect(Collectors.joining());
                if (output.indexOf("include file not found:") > 0) {
                    throw new RuntimeException("Include file(s) not found.");
                }
            });
        });
        extension.getDistribution().getRenderedDocumentation().from(guidesMultiPage);

        assemble.configure(t -> t.dependsOn(extension.getDistribution().getRenderedDocumentation()));

        extension.getBinaries().configureEach(binary -> {
            tasks.register("view" + capitalize(binary.getName()) + "Guide", ViewGuide.class, task -> {
                task.setGroup("Documentation");
                task.setDescription("Generates the guide and open in the browser");
                task.getIndexFile().fileProvider(guidesMultiPage.map(it -> new File(it.getOutputDir(), binary.getPermalink().get() + "/index.html")));
            });
        });

        return guidesMultiPage;
    }

    private void realizeGuides(GuidesInternal extension) {
        // TODO: Disallow changes to published samples container after this point.
        for (GuideInternal guide : extension.getPublishedGuides()) {
            if (guide.getName().contains("_") || guide.getName().contains("-")) {
                throw new IllegalArgumentException(String.format("Guide '%s' has disallowed characters", guide.getName()));
            }

            GuideBinary binary = extension.getBinaries().create(guide.getName());
            binary.getGradleVersion().convention(guide.getMinimumGradleVersion());
            binary.getRepositoryPath().convention(guide.getRepositoryPath());
            binary.getDisplayName().convention(guide.getTitle());
            binary.getGuideDirectory().convention(guide.getGuideDirectory());
            binary.getPermalink().convention(guide.getPermalink());
        }
    }
}
