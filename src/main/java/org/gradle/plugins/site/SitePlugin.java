package org.gradle.plugins.site;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.plugins.site.data.EnvironmentDescriptor;
import org.gradle.plugins.site.data.JavaProjectDescriptor;
import org.gradle.plugins.site.data.ProjectDescriptor;
import org.gradle.plugins.site.data.TaskDescriptor;
import org.gradle.plugins.site.tasks.SiteGenerate;

import java.util.concurrent.Callable;

/**
 * A plugin for generating a web page containing derived project information.
 * <p>
 * The default directory for generated web page is {@code $buildDir/docs/site}.
 */
public class SitePlugin implements Plugin<Project> {

    /**
     * The name of the extension for configuring the runtime behavior of the plugin.
     *
     * @see org.gradle.plugins.site.SitePluginExtension
     */
    public static final String EXTENSION_NAME = "site";

    /**
     * The name of task generating the web page containing derived project information.
     *
     * @see org.gradle.plugins.site.tasks.SiteGenerate
     */
    public static final String GENERATE_SITE_TASK_NAME = "generateSite";

    @Override
    public void apply(final Project project) {
        SitePluginExtension sitePluginExtension = project.getExtensions().create(EXTENSION_NAME, SitePluginExtension.class, project);
        sitePluginExtension.getOutputDir().set(project.getLayout().getBuildDirectory().dir("docs/site"));
        SiteGenerate siteGenerateTask = createSiteTask(project, sitePluginExtension);
        siteGenerateTask.getProjectDescriptor().set(project.provider(new Callable<ProjectDescriptor>() {
            @Override
            public ProjectDescriptor call() throws Exception {
                return deriveProjectDescription(project);
            }
        }));
    }

    private ProjectDescriptor deriveProjectDescription(Project project) {
        ProjectDescriptor projectDescriptor = new ProjectDescriptor(project.getName(), project.getGroup().toString(), project.getDescription(), project.getVersion().toString(), new EnvironmentDescriptor(project.getGradle().getGradleVersion()));
        addPluginDescription(project, projectDescriptor);
        addTasksDescription(project, projectDescriptor);
        addJavaDescription(project, projectDescriptor);
        return projectDescriptor;
    }

    private void addJavaDescription(final Project project, final ProjectDescriptor projectDescriptor) {
        project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            @Override
            public void execute(JavaPlugin javaPlugin) {
                JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                projectDescriptor.setJavaProject(new JavaProjectDescriptor(javaConvention.getSourceCompatibility().toString(), javaConvention.getTargetCompatibility().toString()));
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

    private SiteGenerate createSiteTask(Project project, SitePluginExtension sitePluginExtension) {
        SiteGenerate generateSiteTask = project.getTasks().create(GENERATE_SITE_TASK_NAME, SiteGenerate.class);
        generateSiteTask.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        generateSiteTask.setDescription("Generates a web page containing information about the project.");
        generateSiteTask.getOutputDir().set(sitePluginExtension.getOutputDir());
        generateSiteTask.getCustomData().setWebsiteUrl(sitePluginExtension.getWebsiteUrl());
        generateSiteTask.getCustomData().setVcsUrl(sitePluginExtension.getVcsUrl());
        return generateSiteTask;
    }
}