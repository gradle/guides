package org.gradle.plugins.site;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.plugins.site.data.EnvironmentDescriptor;
import org.gradle.plugins.site.data.JavaProjectDescriptor;
import org.gradle.plugins.site.data.ProjectDescriptor;
import org.gradle.plugins.site.data.TaskDescriptor;
import org.gradle.plugins.site.tasks.SiteGenerate;

import java.io.File;

public class SitePlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "site";
    public static final String SITE_TASK_NAME = "site";

    @Override
    public void apply(Project project) {
        SitePluginExtension sitePluginExtension = project.getExtensions().create(EXTENSION_NAME, SitePluginExtension.class, project);
        sitePluginExtension.setOutputDir(new File(project.getBuildDir(), "docs/site"));

        ProjectDescriptor projectDescriptor = deriveProjectDescription(project);
        configureSiteTask(project, sitePluginExtension, projectDescriptor);
    }

    private ProjectDescriptor deriveProjectDescription(Project project) {
        ProjectDescriptor projectDescriptor = new ProjectDescriptor(project.getName(), project.getGroup().toString(), project.getDescription(), project.getVersion().toString(), new EnvironmentDescriptor(project.getGradle().getGradleVersion()));
        addPluginDescription(project, projectDescriptor);
        addTasksDescription(project, projectDescriptor);
        addJavaDescription(project, projectDescriptor);
        return projectDescriptor;
    }

    private void addJavaDescription(final Project project, final ProjectDescriptor projectDescriptor) {
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project project) {
                project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
                    @Override
                    public void execute(JavaPlugin javaPlugin) {
                        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                        projectDescriptor.setJavaProject(new JavaProjectDescriptor(javaConvention.getSourceCompatibility().toString(), javaConvention.getTargetCompatibility().toString()));
                    }
                });
            }
        });

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
        siteGenerate.getCustomData().setWebsiteUrl(sitePluginExtension.getWebsiteUrlProvider());
        siteGenerate.getCustomData().setVcsUrl(sitePluginExtension.getVcsUrlProvider());
    }
}