package org.gradle.docs.internal;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.docs.internal.components.AssembleDocumentationComponent;
import org.gradle.docs.internal.components.DocumentationArchiveComponent;
import org.gradle.docs.internal.components.TestableAsciidoctorContentComponent;
import org.gradle.docs.internal.configure.ContentBinaries;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import static org.gradle.docs.internal.configure.ArchiveComponents.createTasksForArchiveBinary;

public class DocumentationBasePlugin implements Plugin<Project> {
    public static final String DOCUMENTATION_GROUP_NAME = "Documentation";
    public static final String DOCUMENTATION_EXTENSION_NAME = "documentation";
    public static final String DOCS_TEST_SOURCE_SET_NAME = "docsTest";
    public static final String DOCS_TEST_TASK_NAME = "docsTest";
    public static final String DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME = "docsTestImplementation";

    @Override
    public void apply(Project project) {
        TaskContainer tasks = project.getTasks();
        ProjectLayout layout = project.getLayout();

        project.getPluginManager().apply("base");
        DocumentationExtensionInternal extension = project.getExtensions().create(DOCUMENTATION_EXTENSION_NAME, DocumentationExtensionInternal.class);

        TaskProvider<Task> check = tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME);
        TaskProvider<Task> assemble = tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);

        // Configure binaries
        extension.getComponents().withType(DocumentationArchiveComponent.class).configureEach(binary -> createTasksForArchiveBinary(tasks, layout, binary));
        extension.getComponents().withType(AssembleDocumentationComponent.class).configureEach(attachAssembleTaskToLifecycle(assemble));
        ContentBinaries.createCheckTaskForAsciidoctorContentBinary(project, "checkAsciidoctorContents", extension.getComponents().withType(TestableAsciidoctorContentComponent.class), check, project.getConfigurations().maybeCreate("asciidoctorForDocumentation"));

        // Testing
        configureTesting(project, layout, tasks, check);
    }

    private static Action<AssembleDocumentationComponent> attachAssembleTaskToLifecycle(TaskProvider<Task> lifecycleTask) {
        return component -> lifecycleTask.configure(it -> it.dependsOn(component.getAssembleTask()));
    }

    private void configureTesting(Project project, ProjectLayout layout, TaskContainer tasks, TaskProvider<Task> check) {
        project.getPluginManager().apply("groovy-base");

        SourceSet sourceSet = project.getExtensions().getByType(SourceSetContainer.class).create(DOCS_TEST_SOURCE_SET_NAME);

        TaskProvider<Test> docsTestTask = tasks.register(sourceSet.getName(), Test.class, task -> {
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            task.setDescription("Test documentation");
            task.setTestClassesDirs(sourceSet.getRuntimeClasspath());
            task.setClasspath(sourceSet.getRuntimeClasspath());
            task.setWorkingDir(layout.getProjectDirectory().getAsFile());
        });

        check.configure(task -> task.dependsOn(docsTestTask));
    }
}
