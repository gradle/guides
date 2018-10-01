package org.gradle.plugins.site.generator

import org.gradle.plugins.site.data.CustomData
import org.gradle.plugins.site.data.ProjectDescriptor

/**
 * The site generator used to produce web page.
 *
 * The default implementation of this interface is [org.gradle.plugins.site.generator.FreemarkerSiteGenerator].
 */
interface SiteGenerator {
    fun generate(projectDescriptor: ProjectDescriptor, customData: CustomData)
}
