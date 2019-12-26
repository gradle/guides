package org.gradle.docs.guides.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;

import static org.gradle.docs.internal.StringUtils.toKebabCase;
import static org.gradle.docs.internal.StringUtils.toTitleCase;

public class GuidesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Gradle gradle = project.getGradle();

        project.getPluginManager().apply(DocumentationBasePlugin.class);

        // Configure the guides extension to configure published samples
        GuidesInternal extension = configureGuidesExtension(project);

        // Guides
        extension.getPublishedGuides().configureEach(guide -> applyConventionsForGuides(gradle, guide));
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
}
