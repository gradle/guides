package org.gradle.docs.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.docs.DocumentationExtension;

public class DocumentationBasePlugin implements Plugin<Project> {
    public static final String DOCUMENTATION_EXTENSION_NAME = "documentation";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("lifecycle-base");
        project.getExtensions().create(DOCUMENTATION_EXTENSION_NAME, DocumentationExtension.class);
    }
}
