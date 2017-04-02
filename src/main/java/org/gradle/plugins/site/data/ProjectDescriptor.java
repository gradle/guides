package org.gradle.plugins.site.data;

import org.gradle.api.tasks.Input;

import java.util.ArrayList;
import java.util.List;

public class ProjectDescriptor {

    private final String name;
    private final String group;
    private final String description;
    private final String version;
    private final List<TaskDescriptor> tasks = new ArrayList<TaskDescriptor>();

    public ProjectDescriptor(String name, String group, String description, String version) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.version = version;
    }

    @Input
    public String getName() {
        return name;
    }

    @Input
    public String getGroup() {
        return group;
    }

    @Input
    public String getDescription() {
        return description;
    }

    @Input
    public String getVersion() {
        return version;
    }

    public void addTask(TaskDescriptor task) {
        tasks.add(task);
    }

    @Input
    public List<TaskDescriptor> getTasks() {
        return tasks;
    }
}
