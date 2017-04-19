package org.gradle.plugins.site.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.site.data.CustomData;
import org.gradle.plugins.site.generator.FreemarkerSiteGenerator;
import org.gradle.plugins.site.data.ProjectDescriptor;
import org.gradle.plugins.site.generator.SiteGenerator;

import java.io.File;

public class SiteGenerate extends DefaultTask {

    private final PropertyState<ProjectDescriptor> projectDescriptor;
    private final PropertyState<File> outputDir;
    private final CustomData customData;

    public SiteGenerate() {
        this.projectDescriptor = getProject().property(ProjectDescriptor.class);
        this.outputDir = getProject().property(File.class);
        customData = new CustomData(getProject());
    }

    @Nested
    public ProjectDescriptor getProjectDescriptor() {
        return projectDescriptor.get();
    }

    public void setProjectDescriptor(ProjectDescriptor projectDescriptor) {
        this.projectDescriptor.set(projectDescriptor);
    }

    public void setProjectDescriptor(Provider<ProjectDescriptor> projectDescriptor) {
        this.projectDescriptor.set(projectDescriptor);
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir.get();
    }

    public void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir);
    }

    public void setOutputDir(Provider<File> outputDir) {
        this.outputDir.set(outputDir);
    }

    @Nested
    public CustomData getCustomData() {
        return customData;
    }

    @TaskAction
    public void generate() {
        SiteGenerator siteGenerator = new FreemarkerSiteGenerator(getOutputDir());
        siteGenerator.generate(getProjectDescriptor(), customData);
    }
}
