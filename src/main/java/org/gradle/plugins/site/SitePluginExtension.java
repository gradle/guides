package org.gradle.plugins.site;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.io.File;

/**
 * Configuration options for the {@link org.gradle.plugins.site.SitePlugin}.
 * <p>
 * Below is a full configuration example.
 * <pre>
 * apply plugin: 'org.gradle.site'
 *
 * site {
 *     outputDir = file("$buildDir/site")
 *     websiteUrl = 'http://gradle.org'
 *     vcsUrl = 'https://github.com/gradle-guides/gradle-site-plugin'
 * }
 * </pre>
 */
public class SitePluginExtension {

    private final Property<File> outputDir;
    private final Property<String> websiteUrl;
    private final Property<String> vcsUrl;

    public SitePluginExtension(Project project) {
        outputDir = project.getObjects().property(File.class);
        websiteUrl = project.getObjects().property(String.class);
        vcsUrl = project.getObjects().property(String.class);
    }

    /**
     * Returns the output directory for the generated web page.
     *
     * @return The output directory.
     */
    public File getOutputDir() {
        return outputDir.get();
    }

    /**
     * Returns the {@link org.gradle.api.provider.Provider} calculating the output directory for the generated web page.
     *
     * @return The provider calculating for the output directory.
     */
    public Provider<File> getOutputDirProvider() {
        return outputDir;
    }

    /**
     * Configures the output directory for the generated web page.
     *
     * @param outputDir The output directory.
     */
    public void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir);
    }

    /**
     * Returns the website URL of the project linked in the generated web page.
     *
     * @return The website URL of the project. Maybe be null.
     */
    public String getWebsiteUrl() {
        return websiteUrl.getOrNull();
    }

    /**
     * Returns the {@link org.gradle.api.provider.Provider} calculating the website URL of the project linked in the generated web page.
     *
     * @return The provider calculating the website URL of the project.
     */
    public Provider<String> getWebsiteUrlProvider() {
        return websiteUrl;
    }

    /**
     * Configures the website URL of the project for the generated web page.
     *
     * @param websiteUrl The website URL of the project.
     */
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl.set(websiteUrl);
    }

    /**
     * Returns the version control URL of the project linked in the generated web page.
     *
     * @return The version control URL of the project. Maybe be null.
     */
    public String getVcsUrl() {
        return vcsUrl.getOrNull();
    }

    /**
     * Returns the {@link org.gradle.api.provider.Provider} calculating the version control URL of the project linked in the generated web page.
     *
     * @return The provider calculating the version control URL of the project.
     */
    public Provider<String> getVcsUrlProvider() {
        return vcsUrl;
    }

    /**
     * Configures the version control URL of the project for the generated web page.
     *
     * @param vcsUrl The version control URL of the project.
     */
    public void setVcsUrl(String vcsUrl) {
        this.vcsUrl.set(vcsUrl);
    }
}
