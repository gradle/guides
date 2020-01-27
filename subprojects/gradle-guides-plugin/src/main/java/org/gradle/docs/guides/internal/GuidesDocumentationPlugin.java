package org.gradle.docs.guides.internal;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.guides.internal.tasks.GenerateGuidePageAsciidoc;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.internal.exemplar.AsciidoctorContentTest;
import org.gradle.docs.internal.exemplar.AsciidoctorContentTestConsoleType;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.gradle.docs.internal.Asserts.assertNameDoesNotContainsDisallowedCharacters;
import static org.gradle.docs.internal.DocumentationBasePlugin.DOCUMENTATION_GROUP_NAME;
import static org.gradle.docs.internal.StringUtils.*;
import static org.gradle.docs.internal.configure.AsciidoctorTasks.*;
import static org.gradle.docs.internal.configure.ContentBinaries.*;

public class GuidesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        ProviderFactory providers = project.getProviders();
        ObjectFactory objects = project.getObjects();
        Gradle gradle = project.getGradle();
        Project projectOnlyForCopySpecMethod = project;

        project.getPluginManager().apply(DocumentationBasePlugin.class);
        project.getPluginManager().apply("org.asciidoctor.convert"); // For the `asciidoctor` configuration

        Configuration asciidoctorConfiguration = project.getConfigurations().maybeCreate("asciidoctorForDocumentation");
        project.getRepositories().maven(it -> it.setUrl("https://repo.gradle.org/gradle/libs-releases"));
        project.getDependencies().add(asciidoctorConfiguration.getName(), "org.gradle:docs-asciidoctor-extensions:0.8.0");
        project.getConfigurations().getByName("asciidoctor").extendsFrom(asciidoctorConfiguration);

        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
        TaskProvider<Task> check = tasks.register("checkGuides");

        // Configure the guides extension to configure published samples
        GuidesInternal extension = configureGuidesExtension(project, layout);

        // Guides
        extension.getPublishedGuides().configureEach(guide -> applyConventionsForGuides(extension, gradle, guide));

        // Guide binaries
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().withType(GuideContentBinary.class).all(binary -> createTasksForContentBinary(tasks, binary));
        extension.getBinaries().withType(GuideContentBinary.class).all(binary -> createCheckTasksForContentBinary(tasks, binary, check));
        extension.getBinaries().withType(GuideContentBinary.class).all(binary -> createTasksForGuideContentBinary(tasks, layout, providers, binary));

        // Render all the documentation out to HTML
        TaskProvider<? extends Task> renderTask = renderGuidesDocumentation(tasks, assemble, check, extension);

        // Publish the guides to consumers
        createPublishGuidesElements(project.getConfigurations(), objects, renderTask, extension);

        // Testing
        createCheckTaskForAsciidoctorContentBinary(project, "checkAsciidoctorGuideContents", extension.getBinaries().withType(TestableAsciidoctorGuideContentBinary.class), check, asciidoctorConfiguration);

        // Trigger everything by realizing guide container
        project.afterEvaluate(p -> realizeGuides(extension, objects, projectOnlyForCopySpecMethod));
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
        guide.getDisplayName().convention(toTitleCase(name));
        guide.getDescription().convention("");
        guide.getCategory().convention("Uncategorized");
        guide.getPermalink().convention(toSnakeCase(guide.getName()));
        guide.getGuideName().convention(name);
    }

    private void createTasksForGuideContentBinary(TaskContainer tasks, ProjectLayout layout, ProviderFactory providers, GuideContentBinary binary) {
        TaskProvider<GenerateGuidePageAsciidoc> generateGuidePage = tasks.register("generate" + capitalize(binary.getName()) + "Page", GenerateGuidePageAsciidoc.class, task -> {
            task.setDescription("Generates asciidoc page for guide '" + binary.getName() + "'");

            // TODO: Attributes is an extra property compared to samples
            // TODO: Input/output property name differ a bit
            task.getAttributes().empty();
            task.getAttributes().put("samplescodedir", binary.getGuideDirectory().file("samples/code").map(it -> it.getAsFile().getAbsolutePath()));
            task.getAttributes().put("samplesoutputdir", binary.getGuideDirectory().file("samples/output").map(it -> it.getAsFile().getAbsolutePath()));
            task.getAttributes().put("samples-dir", binary.getGuideDirectory().file("samples").map(it -> it.getAsFile().getAbsolutePath()));
            task.getAttributes().put("gradle-version", binary.getGradleVersion());
            task.getAttributes().put("user-manual", binary.getGradleVersion().map(v -> "https://docs.gradle.org/" + v + "/userguide/"));
            task.getAttributes().put("language-reference", binary.getGradleVersion().map(v -> "https://docs.gradle.org/" + v + "/dsl/"));
            task.getAttributes().put("api-reference", binary.getGradleVersion().map(v -> "https://docs.gradle.org/" + v + "/javadoc/"));
            task.getAttributes().put("repository-path", binary.getRepositoryPath());
            task.getAttributes().put("guide-name", binary.getGuideName());
            task.getIndexFile().convention(binary.getSourcePageFile());
            task.getOutputFile().fileProvider(providers.provider(() -> new File(task.getTemporaryDir(), "index.adoc")));
        });
        binary.getIndexPageFile().convention(generateGuidePage.flatMap(GenerateGuidePageAsciidoc::getOutputFile));
    }

    private TaskProvider<? extends Task> renderGuidesDocumentation(TaskContainer tasks, TaskProvider<Task> assemble, TaskProvider<Task> check, GuidesInternal extension) {
        TaskProvider<Sync> assembleDocs = tasks.register("assembleGuides", Sync.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Assembles all intermediate files needed to generate the samples documentation.");

            extension.getBinaries().withType(GuideContentBinary.class).forEach(binary -> {
                // TODO: This is extra content compared to Samples
                task.from(binary.getGuideDirectory().dir("contents"), sub -> {
                    sub.into(binary.getBaseDirectory());
                    sub.include("**/*.adoc");
                    sub.include("**/*.txt");
                });
                task.from(binary.getIndexPageFile(), sub -> sub.into(binary.getBaseDirectory()));
            });
            task.into(extension.getDocumentationInstallRoot());
        });

        extension.getBinaries().withType(GuideContentBinary.class).configureEach(binary -> {
            binary.getInstalledIndexPageFile().fileProvider(assembleDocs.map(task -> new File(task.getDestinationDir(), binary.getSourcePermalink().get())));
        });

        TaskProvider<AsciidoctorTask> guidesMultiPage = tasks.register("guidesMultiPage", AsciidoctorTask.class, task -> {
            task.getInputs().files("samples").withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();

            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Generates multi-page guides index.");
            task.dependsOn(assembleDocs);
            Map<String, Object> attributes = new HashMap<>(genericAttributes());

            cleanStaleFiles(task);
            configureResources(task, extension.getBinaries().withType(GuideContentBinary.class));
            configureSources(task, extension.getBinaries().withType(GuideContentBinary.class));

            // TODO: This breaks the provider
            task.setSourceDir(extension.getDocumentationInstallRoot().get().getAsFile());
            // TODO: This breaks the provider
            task.setOutputDir(extension.getRenderedDocumentationRoot().get().getAsFile());

            task.setSeparateOutputDirs(false);

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
        });
        extension.getDistribution().getRenderedDocumentation().from(guidesMultiPage);

        assemble.configure(t -> t.dependsOn(extension.getDistribution().getRenderedDocumentation()));

        extension.getBinaries().withType(GuideContentBinary.class).configureEach(binary -> {
            binary.getRenderedPageFile().fileProvider(guidesMultiPage.map(it -> new File(it.getOutputDir(), binary.getRenderedPermalink().get())));
            binary.getViewablePageFile().fileProvider(guidesMultiPage.map(it -> new File(it.getOutputDir(), binary.getRenderedPermalink().get())));
        });

        return guidesMultiPage;
    }

    private void realizeGuides(GuidesInternal extension, ObjectFactory objects, Project project) {
        // TODO: Disallow changes to published samples container after this point.
        for (GuideInternal guide : extension.getPublishedGuides()) {
            assertNameDoesNotContainsDisallowedCharacters(guide, "Guide '%s' has disallowed characters", guide.getName());

            GuideContentBinary contentBinary = objects.newInstance(GuideContentBinary.class, guide.getName());
            extension.getBinaries().add(contentBinary);
            contentBinary.getGradleVersion().convention(guide.getMinimumGradleVersion());
            contentBinary.getRepositoryPath().convention(guide.getRepositoryPath());
            contentBinary.getDisplayName().convention(guide.getDisplayName());
            contentBinary.getGuideDirectory().convention(guide.getGuideDirectory());
            contentBinary.getBaseDirectory().convention(guide.getPermalink());
            // TODO: Maybe have a source permalink and rendered permalink for adoc and html respectively
            contentBinary.getRenderedPermalink().convention(contentBinary.getBaseDirectory().map(baseDirectory -> baseDirectory + "/index.html"));
            contentBinary.getSourcePermalink().convention(contentBinary.getBaseDirectory().map(baseDirectory -> baseDirectory + "/index.adoc"));
            contentBinary.getResourceFiles().from(guide.getGuideDirectory().dir("contents/images"));
            contentBinary.getResourceSpec().convention(project.copySpec(spec -> spec.from(guide.getGuideDirectory().dir("contents/images"), it -> it.into(contentBinary.getBaseDirectory().get() + "/images"))));
            contentBinary.getSourcePattern().convention(contentBinary.getSourcePermalink());
            contentBinary.getSourcePageFile().convention(guide.getGuideDirectory().file("contents/index.adoc"));
            contentBinary.getGuideName().convention(guide.getGuideName());

            TestableAsciidoctorGuideContentBinary testableAsciidoctorGuideContentBinary = objects.newInstance(TestableAsciidoctorGuideContentBinary.class, guide.getName());
            extension.getBinaries().add(testableAsciidoctorGuideContentBinary);
            testableAsciidoctorGuideContentBinary.getContentFile().convention(contentBinary.getInstalledIndexPageFile());
        }
    }
}
