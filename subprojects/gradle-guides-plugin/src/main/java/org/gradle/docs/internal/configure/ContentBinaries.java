package org.gradle.docs.internal.configure;

import org.gradle.api.tasks.TaskContainer;
import org.gradle.docs.internal.ViewableContentBinary;
import org.gradle.docs.internal.tasks.ViewDocumentation;

import static org.gradle.docs.internal.DocumentationBasePlugin.DOCUMENTATION_GROUP_NAME;

public class ContentBinaries {
    public static void createTasksForContentBinary(TaskContainer tasks, ViewableContentBinary binary) {
        tasks.register(binary.getViewTaskName(), ViewDocumentation.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Open in the browser the rendered documentation");
            task.getIndexFile().convention(binary.getViewablePageFile());
        });
    }
}
