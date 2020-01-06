package org.gradle.docs.internal.configure;

import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.internal.TestableContentBinary;
import org.gradle.docs.internal.ViewableContentBinary;
import org.gradle.docs.internal.tasks.CheckLinks;
import org.gradle.docs.internal.tasks.ViewDocumentation;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import static org.gradle.docs.internal.DocumentationBasePlugin.DOCUMENTATION_GROUP_NAME;
import static org.gradle.docs.internal.StringUtils.capitalize;

public class ContentBinaries {
    public static void createTasksForContentBinary(TaskContainer tasks, ViewableContentBinary binary) {
        tasks.register(binary.getViewTaskName(), ViewDocumentation.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Open in the browser the rendered documentation");
            task.getIndexFile().convention(binary.getViewablePageFile());
        });
    }

    public static void createCheckTasksForContentBinary(TaskContainer tasks, TestableContentBinary binary, TaskProvider<Task> check) {
        TaskProvider<CheckLinks> checkLinksTask = tasks.register(binary.getCheckLinksTaskName(), CheckLinks.class, task -> {
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            task.setDescription("Check for any dead link in the rendered documentation");
            task.getIndexDocument().convention(binary.getRenderedPageFile());
        });

        check.configure(it -> it.dependsOn(checkLinksTask));
    }
}
