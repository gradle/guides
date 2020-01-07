package org.gradle.docs.samples.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.Template;
import org.gradle.docs.samples.internal.tasks.SyncWithProvider;

import static org.gradle.docs.internal.StringUtils.capitalize;
import static org.gradle.docs.internal.StringUtils.toKebabCase;

@SuppressWarnings("UnstableApiUsage")
public class SamplesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        TaskContainer tasks = project.getTasks();

        project.getPluginManager().apply(SamplesDocumentationPlugin.class);

        // Register a samples extension to configure published samples
        SamplesInternal extension = configureSamplesExtension(project, layout);

        // Templates
        extension.getTemplates().configureEach(template -> applyConventionsForTemplates(extension, template));
        extension.getTemplates().all(template -> createTasksForTemplates(layout, tasks, template));
    }

    private void createTasksForTemplates(ProjectLayout layout, TaskContainer tasks, Template template) {
        TaskProvider<SyncWithProvider> generateTemplate = tasks.register("generateTemplate" + capitalize(template.getName()), SyncWithProvider.class, task -> {
            task.setDescription("Generates template into a target directory.");
            task.from(template.getSourceDirectory(), copySpec -> copySpec.into(template.getTarget()));
            task.into(layout.getBuildDirectory().dir("tmp/" + task.getName()));
        });

        template.getTemplateDirectory().convention(generateTemplate.flatMap(SyncWithProvider::getDestinationDirectory));
    }

    private void applyConventionsForTemplates(Samples extension, Template template) {
        template.getTarget().convention("");
        template.getSourceDirectory().convention(extension.getTemplatesRoot().dir(toKebabCase(template.getName())));
    }

    private SamplesInternal configureSamplesExtension(Project project, ProjectLayout layout) {
        SamplesInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSamples();

        project.getExtensions().add(Samples.class, "samples", extension);
        extension.getSamplesRoot().set(layout.getProjectDirectory().dir("src/samples"));

        extension.getTemplatesRoot().convention(layout.getProjectDirectory().dir("src/samples/templates"));

        return extension;
    }
}
