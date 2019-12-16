package org.gradle.plugins.site

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.site.data.EnvironmentDescriptor
import org.gradle.plugins.site.data.JavaProjectDescriptor
import org.gradle.plugins.site.data.ProjectDescriptor
import org.gradle.plugins.site.data.TaskDescriptor
import org.gradle.plugins.site.tasks.SiteGenerate

class SitePlugin : Plugin<Project> {
    /**
     * The name of the extension for configuring the runtime behavior of the plugin.
     *
     * @see org.gradle.plugins.site.SitePluginExtension
     */
    val EXTENSION_NAME = "site"

    /**
     * The name of task generating the web page containing derived project information.
     *
     * @see org.gradle.plugins.site.tasks.SiteGenerate
     */
    val GENERATE_SITE_HTML_TASK_NAME = "generateSiteHtml"

    val DEFAULT_OUTPUT_DIR = "docs/site"

    override fun apply(project: Project): Unit = project.run {
        val sitePluginExtension = extensions.create(EXTENSION_NAME, SitePluginExtension::class.java, project)
        sitePluginExtension.outputDir.set(layout.buildDirectory.dir(DEFAULT_OUTPUT_DIR))

        registerSiteTask(sitePluginExtension).configure { siteGenerate ->
            siteGenerate.projectDescriptor.set(provider {
                deriveProjectDescription()
            })
        }
    }

    private fun Project.deriveProjectDescription(): ProjectDescriptor {
        val projectDescriptor = ProjectDescriptor(name, group.toString(), description.orEmpty(), version.toString(), EnvironmentDescriptor(gradle.gradleVersion))
        addPluginDescription(projectDescriptor)
        addTasksDescription(projectDescriptor)
        addJavaDescription(projectDescriptor)
        return projectDescriptor
    }

    private fun Project.addPluginDescription(projectDescriptor: ProjectDescriptor) {
        plugins.all { plugin -> projectDescriptor.addPluginClass(plugin.javaClass) }
    }

    private fun Project.addTasksDescription(projectDescriptor: ProjectDescriptor) {
        tasks.all { task ->
            if (task.group != null) {
                projectDescriptor.addTask(TaskDescriptor(task.name, task.path, task.group!!, task.description ?: ""))
            }
        }
    }

    private fun Project.addJavaDescription(projectDescriptor: ProjectDescriptor) {
        plugins.withType(JavaPlugin::class.java) {
            val java = extensions.getByType(JavaPluginExtension::class.java)
            projectDescriptor.javaProject = JavaProjectDescriptor(java.sourceCompatibility.toString(), java.targetCompatibility.toString())
        }
    }

    private fun Project.registerSiteTask(sitePluginExtension: SitePluginExtension): TaskProvider<SiteGenerate> =
            tasks.register(GENERATE_SITE_HTML_TASK_NAME, SiteGenerate::class.java) { siteGenerate ->
                siteGenerate.group = JavaBasePlugin.DOCUMENTATION_GROUP
                siteGenerate.description = "Generates a web page containing information about the project."
                siteGenerate.outputDir.set(sitePluginExtension.outputDir)
                siteGenerate.customData.setWebsiteUrl(sitePluginExtension.websiteUrl)
                siteGenerate.customData.setVcsUrl(sitePluginExtension.vcsUrl)
            }
}
