package org.gradle.plugins.site

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.plugins.site.data.EnvironmentDescriptor
import org.gradle.plugins.site.data.JavaProjectDescriptor
import org.gradle.plugins.site.data.ProjectDescriptor
import org.gradle.plugins.site.data.TaskDescriptor
import org.gradle.plugins.site.tasks.SiteGenerate

open class SitePlugin : Plugin<Project> {
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
    val GENERATE_SITE_TASK_NAME = "generateSite"

    val DEFAULT_OUTPUT_DIR = "docs/site"

    override fun apply(project: Project) {
        val sitePluginExtension = project.extensions.create(EXTENSION_NAME, SitePluginExtension::class.java, project)
        sitePluginExtension.outputDir.set(project.layout.buildDirectory.dir(DEFAULT_OUTPUT_DIR))

        val siteGenerate = createSiteTask(project, sitePluginExtension)

        siteGenerate.projectDescriptor.set(project.provider {
            deriveProjectDescription(project)
        })
    }

    private fun deriveProjectDescription(project: Project): ProjectDescriptor {
        val projectDescriptor = ProjectDescriptor(project.name, project.group.toString(), project.description.orEmpty(), project.version.toString(), EnvironmentDescriptor(project.gradle.gradleVersion))
        addPluginDescription(project, projectDescriptor)
        addTasksDescription(project, projectDescriptor)
        addJavaDescription(project, projectDescriptor)
        return projectDescriptor
    }

    private fun addPluginDescription(project: Project, projectDescriptor: ProjectDescriptor) {
        project.plugins.all { plugin -> projectDescriptor.addPluginClass(plugin.javaClass) }
    }

    private fun addTasksDescription(project: Project, projectDescriptor: ProjectDescriptor) {
        project.tasks.all { task ->
            val description = if (task.description != null) task.description!! else ""

            if (task.group != null) {
                projectDescriptor.addTask(TaskDescriptor(task.name, task.path, task.group!!, description))
            }
        }
    }

    private fun addJavaDescription(project: Project, projectDescriptor: ProjectDescriptor) {
        project.plugins.withType(JavaPlugin::class.java) {
            val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
            projectDescriptor.javaProject = JavaProjectDescriptor(javaConvention.sourceCompatibility.toString(), javaConvention.targetCompatibility.toString())
        }
    }

    private fun createSiteTask(project: Project, sitePluginExtension: SitePluginExtension): SiteGenerate {
        val siteGenerate = project.tasks.create(GENERATE_SITE_TASK_NAME, SiteGenerate::class.java)
        siteGenerate.group = JavaBasePlugin.DOCUMENTATION_GROUP
        siteGenerate.description = "Generates a web page containing information about the project."
        siteGenerate.outputDir.set(sitePluginExtension.outputDir)
        siteGenerate.customData.setWebsiteUrl(sitePluginExtension.websiteUrl)
        siteGenerate.customData.setVcsUrl(sitePluginExtension.vcsUrl)

        return siteGenerate
    }
}
