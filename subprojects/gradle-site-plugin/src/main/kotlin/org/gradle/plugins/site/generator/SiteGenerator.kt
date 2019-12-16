package org.gradle.plugins.site.generator

import org.gradle.plugins.site.data.ProjectDescriptor
import org.gradle.plugins.site.data.ProjectLinksDescriptor

/**
 * The site generator used to produce web page.
 *
 * The default implementation of this interface is [org.gradle.plugins.site.generator.FreemarkerSiteGenerator].
 */
interface SiteGenerator {
    fun generate(projectDescriptor: ProjectDescriptor, projectLinksDescriptor: ProjectLinksDescriptor)
}
