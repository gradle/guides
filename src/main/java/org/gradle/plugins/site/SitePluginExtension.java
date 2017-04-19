package org.gradle.plugins.site;

import org.gradle.api.Project;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;

import java.io.File;

public class SitePluginExtension {

    private final PropertyState<File> outputDir;
    private final PropertyState<String> websiteUrl;
    private final PropertyState<String> vcsUrl;

    public SitePluginExtension(Project project) {
        outputDir = project.property(File.class);
        websiteUrl = project.property(String.class);
        vcsUrl = project.property(String.class);
    }

    public File getOutputDir() {
        return outputDir.get();
    }

    public Provider<File> getOutputDirProvider() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir);
    }

    public String getWebsiteUrl() {
        return websiteUrl.getOrNull();
    }

    public Provider<String> getWebsiteUrlProvider() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl.set(websiteUrl);
    }

    public String getVcsUrl() {
        return vcsUrl.getOrNull();
    }

    public Provider<String> getVcsUrlProvider() {
        return vcsUrl;
    }

    public void setVcsUrl(String vcsUrl) {
        this.vcsUrl.set(vcsUrl);
    }
}
