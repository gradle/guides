package org.gradle.docs.samples.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.docs.internal.DocumentationBasePlugin;

public class SamplesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(DocumentationBasePlugin.class);
    }
}
