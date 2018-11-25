package org.gradle.plugins.site.data

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.io.Serializable

class CustomData(project: Project) {
    private val websiteUrl: Property<String> = project.objects.property(String::class.java)
    private val vcsUrl: Property<String> = project.objects.property(String::class.java)

    @Input
    @Optional
    fun getWebsiteUrl(): String? {
        return websiteUrl.orNull
    }

    fun setWebsiteUrl(websiteUrl: Provider<String>) {
        this.websiteUrl.set(websiteUrl)
    }

    @Input
    @Optional
    fun getVcsUrl(): String? {
        return vcsUrl.orNull
    }

    fun setVcsUrl(vcsUrl: Provider<String>) {
        this.vcsUrl.set(vcsUrl)
    }
}

// Separate data class for URLs because `Property` (in CustomData) is not serializable
data class ProjectLinksDescriptor(@get:Input val websiteUrl: String?, @get:Input val vcsUrl: String?) : Serializable

data class EnvironmentDescriptor(@get:Input val gradleVersion: String) : Serializable

data class JavaProjectDescriptor(@get:Input val sourceCompatibility: String,
                            @get:Input val targetCompatibility: String) : Serializable

data class ProjectDescriptor(@get:Input val name: String,
                        @get:Input val group: String,
                        @get:Input @get:Optional val description: String,
                        @get:Input val version: String,
                        @get:Nested val environment: EnvironmentDescriptor) : Serializable {

    private val pluginClasses = mutableListOf<Class<out Plugin<*>>>()
    private val tasks = mutableListOf<TaskDescriptor>()

    @get:Nested
    @get:Optional
    var javaProject: JavaProjectDescriptor? = null

    @Input
    fun getTasks(): List<TaskDescriptor> {
        return tasks
    }

    fun addTask(task: TaskDescriptor) {
        tasks.add(task)
    }

    @Input
    fun getPluginClasses(): List<Class<out Plugin<*>>> {
        return pluginClasses
    }

    fun addPluginClass(pluginClass: Class<out Plugin<*>>) {
        this.pluginClasses.add(pluginClass)
    }
}

data class TaskDescriptor(@get:Input val name: String,
                     @get:Input val path: String,
                     @get:Input val group: String,
                     @get:Input val description: String) : Serializable
