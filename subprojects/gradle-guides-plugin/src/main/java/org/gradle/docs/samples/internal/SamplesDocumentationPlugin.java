package org.gradle.docs.samples.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.samples.Dsl;
import org.gradle.docs.samples.Samples;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.StringUtils.*;

public class SamplesDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();

        project.getPluginManager().apply(DocumentationBasePlugin.class);

        // Register a samples extension to configure published samples
        SamplesInternal extension = configureSamplesExtension(project, layout);

        // Samples
        extension.getPublishedSamples().configureEach(sample -> applyConventionsForSamples(extension, sample));

        // Trigger everything by realizing sample container
        project.afterEvaluate(p -> realizeSamples(extension));
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

    private void realizeSamples(SamplesInternal extension) {
        // TODO: Disallow changes to published samples container after this point.
        for (SampleInternal sample : extension.getPublishedSamples()) {
            if (sample.getName().contains("_") || sample.getName().contains("-")) {
                throw new IllegalArgumentException(String.format("Sample '%s' has disallowed characters", sample.getName()));
            }
        }
    }
}
