package org.gradle.plugins.site;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.plugins.site.data.ProjectDescriptor;
import org.gradle.plugins.site.data.TaskDescriptor;
import org.gradle.plugins.site.tasks.SiteGenerate;

import java.io.File;

public class SitePlugin implements Plugin<Project> {

    public static final String SITE_TASK_NAME = "site";

    @Override
    public void apply(Project project) {
        SitePluginExtension sitePluginExtension = project.getExtensions().create("site", SitePluginExtension.class, project);
        sitePluginExtension.setOutputDir(new File(project.getBuildDir(), "docs/site"));

        ProjectDescriptor projectDescriptor = deriveProjectDescription(project);
        configureSiteTask(project, sitePluginExtension, projectDescriptor);
    }

    private ProjectDescriptor deriveProjectDescription(Project project) {
        ProjectDescriptor projectDescriptor = new ProjectDescriptor(project.getName(), project.getGroup().toString(), project.getDescription(), project.getVersion().toString());
        addPluginDescription(project, projectDescriptor);
        addTasksDescription(project, projectDescriptor);
        return projectDescriptor;
    }

    private void addPluginDescription(Project project, final ProjectDescriptor projectDescriptor) {
        project.getPlugins().all(new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                projectDescriptor.addPluginClass(plugin.getClass());
            }
        });
    }

    private void addTasksDescription(Project project, final ProjectDescriptor projectDescriptor) {
        project.getTasks().all(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (task.getGroup() != null) {
                    projectDescriptor.addTask(new TaskDescriptor(task.getName(), task.getPath(), task.getGroup(), task.getDescription()));
                }
            }
        });
    }

    private void configureSiteTask(Project project, SitePluginExtension sitePluginExtension, ProjectDescriptor projectDescriptor) {
        SiteGenerate siteGenerate = project.getTasks().create(SITE_TASK_NAME, SiteGenerate.class);
        siteGenerate.setProjectDescriptor(projectDescriptor);
        siteGenerate.setOutputDir(sitePluginExtension.getOutputDirProvider());
    }
}