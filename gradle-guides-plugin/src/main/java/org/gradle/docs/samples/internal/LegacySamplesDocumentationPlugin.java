package org.gradle.docs.samples.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.samples.Samples;

@SuppressWarnings("UnstableApiUsage")
public class LegacySamplesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SamplesDocumentationPlugin.class);

        // Register a samples extension to configure published samples
        SamplesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSamples();

        project.getExtensions().add(Samples.class, "samples", extension);
        extension.getSamplesRoot().set(project.getLayout().getProjectDirectory().dir("src/samples"));
        extension.getTemplatesRoot().convention(project.getLayout().getProjectDirectory().dir("src/samples/templates"));

    }
}
