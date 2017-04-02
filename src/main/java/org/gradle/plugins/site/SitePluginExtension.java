package org.gradle.plugins.site;

import org.gradle.api.Project;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;

import java.io.File;

public class SitePluginExtension {

    private final PropertyState<File> outputDir;

    public SitePluginExtension(Project project) {
        outputDir = project.property(File.class);
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
}
