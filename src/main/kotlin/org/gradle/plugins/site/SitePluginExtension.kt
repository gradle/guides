package org.gradle.plugins.site

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

open class SitePluginExtension(project: Project) {
    /**
     * Returns the output directory for the generated web page.
     *
     * @return The output directory property.
     */
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    /**
     * Returns the website URL of the project linked in the generated web page.
     *
     * @return The website URL of the project property.
     */
    val websiteUrl: Property<String> = project.objects.property(String::class.java)

    /**
     * Returns the version control URL of the project linked in the generated web page.
     *
     * @return The version control URL of the project property.
     */
    val vcsUrl: Property<String> = project.objects.property(String::class.java)
}
