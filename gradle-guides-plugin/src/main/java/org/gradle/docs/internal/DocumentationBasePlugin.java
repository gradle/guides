package org.gradle.docs.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

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
        project.getExtensions().create(DOCUMENTATION_EXTENSION_NAME, DocumentationExtensionInternal.class);

        TaskProvider<Task> check = tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME);

        // Testing
        configureTesting(project, layout, tasks, check);
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
