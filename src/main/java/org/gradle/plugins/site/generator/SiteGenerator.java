package org.gradle.plugins.site.generator;

import org.gradle.plugins.site.data.CustomData;
import org.gradle.plugins.site.data.ProjectDescriptor;

/**
 * The site generator used to produce web page.
 * <p>
 * The default implementation of this interface is {@link org.gradle.plugins.site.generator.FreemarkerSiteGenerator}.
 */
public interface SiteGenerator {

    void generate(ProjectDescriptor projectDescriptor, CustomData customData);
}
