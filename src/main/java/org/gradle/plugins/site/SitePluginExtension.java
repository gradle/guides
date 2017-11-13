package org.gradle.plugins.site;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

/**
 * Configuration options for the {@link org.gradle.plugins.site.SitePlugin}.
 * <p>
 * Below is a full configuration example.
 * <pre>
 * apply plugin: 'org.gradle.site'
 *
 * site {
 *     outputDir = layout.buildDirectory.dir("site")
 *     websiteUrl = 'http://gradle.org'
 *     vcsUrl = 'https://github.com/gradle-guides/gradle-site-plugin'
 * }
 * </pre>
 */
public class SitePluginExtension {

    private final DirectoryProperty outputDir;
    private final Property<String> websiteUrl;
    private final Property<String> vcsUrl;

    public SitePluginExtension(Project project) {
        outputDir = project.getLayout().directoryProperty();
        websiteUrl = project.getObjects().property(String.class);
        vcsUrl = project.getObjects().property(String.class);
    }

    /**
     * Returns the output directory for the generated web page.
     *
     * @return The output directory property.
     */
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    /**
     * Returns the website URL of the project linked in the generated web page.
     *
     * @return The website URL of the project property.
     */
    public Property<String> getWebsiteUrl() {
        return websiteUrl;
    }

    /**
     * Returns the version control URL of the project linked in the generated web page.
     *
     * @return The version control URL of the project property.
     */
    public Property<String> getVcsUrl() {
        return vcsUrl;
    }
}
