package org.gradle.plugins.site.data;

import org.gradle.api.tasks.Input;

import java.io.Serializable;

/**
 * The data descriptor for task information.
 */
public class TaskDescriptor implements Serializable {

    private final String name;
    private final String path;
    private final String group;
    private final String description;

    public TaskDescriptor(String name, String path, String group, String description) {
        this.name = name;
        this.path = path;
        this.group = group;
        this.description = description;
    }

    @Input
    public String getName() {
        return name;
    }

    @Input
    public String getPath() {
        return path;
    }

    @Input
    public String getGroup() {
        return group;
    }

    @Input
    public String getDescription() {
        return description;
    }
}
