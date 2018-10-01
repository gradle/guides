package org.gradle.plugins.site.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.site.data.CustomData
import org.gradle.plugins.site.data.ProjectDescriptor
import org.gradle.plugins.site.generator.FreemarkerSiteGenerator

open class SiteGenerate : DefaultTask() {

    /**
     * Returns the project descriptor containing the derived project information.
     *
     * @return The project descriptor.
     */
    @get:Nested
    val projectDescriptor: Property<ProjectDescriptor> = project.objects.property(ProjectDescriptor::class.java)

    /**
     * Returns the output directory for the generated web page.
     *
     * @return The output directory.
     */
    @get:OutputDirectory
    val outputDir: DirectoryProperty = newOutputDirectory()

    /**
     * Returns the custom data to be used in the generated web page.
     *
     * @return The custom data.
     */
    @get:Nested
    val customData: CustomData = CustomData(project)

    @TaskAction
    fun generate() {
        val siteGenerator = FreemarkerSiteGenerator(outputDir.get().asFile)
        siteGenerator.generate(projectDescriptor.get(), customData)
    }
}
