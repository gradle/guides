package org.gradle.plugins.site.data;

import org.gradle.api.Plugin;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;

import java.util.ArrayList;
import java.util.List;

public class ProjectDescriptor {

    private final String name;
    private final String group;
    private final String description;
    private final String version;
    private final List<Class<? extends Plugin>> pluginClasses = new ArrayList<Class<? extends Plugin>>();
    private final List<TaskDescriptor> tasks = new ArrayList<TaskDescriptor>();
    private JavaProjectDescriptor javaProject;

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

    @Input
    public List<TaskDescriptor> getTasks() {
        return tasks;
    }

    public void addTask(TaskDescriptor task) {
        tasks.add(task);
    }

    @Input
    public List<Class<? extends Plugin>> getPluginClasses() {
        return pluginClasses;
    }

    public void addPluginClass(Class<? extends Plugin> pluginClass) {
        this.pluginClasses.add(pluginClass);
    }

    @Nested
    public JavaProjectDescriptor getJavaProject() {
        return javaProject;
    }

    public void setJavaProject(JavaProjectDescriptor javaProject) {
        this.javaProject = javaProject;
    }
}
