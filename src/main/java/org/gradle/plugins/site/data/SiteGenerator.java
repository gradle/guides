package org.gradle.plugins.site.data;

public interface SiteGenerator {

    void generate(ProjectDescriptor projectDescriptor, CustomData customData);
}
