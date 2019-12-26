package org.gradle.docs.guides.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;

import static org.gradle.docs.internal.StringUtils.toKebabCase;
import static org.gradle.docs.internal.StringUtils.toTitleCase;

public class GuidesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();
        Gradle gradle = project.getGradle();

        project.getPluginManager().apply(DocumentationBasePlugin.class);

        // Configure the guides extension to configure published samples
        GuidesInternal extension = configureGuidesExtension(project);

        // Guides
        extension.getPublishedGuides().configureEach(guide -> applyConventionsForGuides(gradle, guide));

        // Guide binaries
        // TODO: This could be lazy if we had a way to make the TaskContainer require evaluation
        extension.getBinaries().all(binary -> createTasksForGuideBinary(tasks, layout, binary));

        // Trigger everything by realizing guide container
        project.afterEvaluate(p -> realizeGuides(extension));
    }

    private GuidesInternal configureGuidesExtension(Project project) {
        GuidesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getGuides();
        return extension;
    }

    private void applyConventionsForGuides(Gradle gradle, GuideInternal guide) {
        String name = guide.getName();
        guide.getRepositoryPath().convention("gradle-guides/" + toKebabCase(name));
        guide.getMinimumGradleVersion().convention(gradle.getGradleVersion());
        guide.getTitle().convention(toTitleCase(name));
        guide.getDescription().convention("");
        guide.getCategory().convention("Uncategorized");
    }

    private void createTasksForGuideBinary(TaskContainer tasks, ProjectLayout layout, GuideBinary binary) {

    }

    private void realizeGuides(GuidesInternal extension) {
        // TODO: Disallow changes to published samples container after this point.
        for (GuideInternal guide : extension.getPublishedGuides()) {
            if (guide.getName().contains("_") || guide.getName().contains("-")) {
                throw new IllegalArgumentException(String.format("Guide '%s' has disallowed characters", guide.getName()));
            }

            extension.getBinaries().create(guide.getName() + "Content");
        }
    }
}
