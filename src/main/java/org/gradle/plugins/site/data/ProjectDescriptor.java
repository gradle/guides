package org.gradle.plugins.site.data;

import org.gradle.api.Plugin;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import java.util.ArrayList;
import java.util.List;

/**
 * The data descriptor for the whole project.
 */
public class ProjectDescriptor {

    private final String name;
    private final String group;
    private final String description;
    private final String version;
    private final List<Class<? extends Plugin>> pluginClasses = new ArrayList<Class<? extends Plugin>>();
    private final List<TaskDescriptor> tasks = new ArrayList<TaskDescriptor>();
    private final EnvironmentDescriptor environment;
    private JavaProjectDescriptor javaProject;

    public ProjectDescriptor(String name, String group, String description, String version, EnvironmentDescriptor environment) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.version = version;
        this.environment = environment;
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
    @Optional
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
    @Optional
    public JavaProjectDescriptor getJavaProject() {
        return javaProject;
    }

    public void setJavaProject(JavaProjectDescriptor javaProject) {
        this.javaProject = javaProject;
    }

    @Nested
    public EnvironmentDescriptor getEnvironment() {
        return environment;
    }
}
