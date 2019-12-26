package org.gradle.docs.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.docs.guides.internal.GuidesDocumentationPlugin;

public class BuildDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GuidesDocumentationPlugin.class);
    }
}
