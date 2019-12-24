package org.gradle.docs.guides.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.docs.internal.DocumentationBasePlugin;

public class GuidesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(DocumentationBasePlugin.class);
    }
}
