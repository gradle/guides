package org.gradle.plugins.site.generator;

import org.gradle.plugins.site.data.CustomData;
import org.gradle.plugins.site.data.ProjectDescriptor;

public interface SiteGenerator {

    void generate(ProjectDescriptor projectDescriptor, CustomData customData);
}
