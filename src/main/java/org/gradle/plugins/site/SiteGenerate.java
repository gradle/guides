package org.gradle.plugins.site;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SiteGenerate extends DefaultTask {

    private final PropertyState<ProjectDescriptor> projectDescriptor;
    private final PropertyState<File> outputDir;

    public SiteGenerate() {
        this.projectDescriptor = getProject().property(ProjectDescriptor.class);
        this.outputDir = getProject().property(File.class);
    }

    @Nested
    public ProjectDescriptor getProjectDescriptor() {
        return projectDescriptor.get();
    }

    public void setProjectDescriptor(ProjectDescriptor projectDescriptor) {
        this.projectDescriptor.set(projectDescriptor);
    }

    public void setProjectDescriptor(Provider projectDescriptor) {
        this.projectDescriptor.set(projectDescriptor);
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir.get();
    }

    public void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir);
    }

    public void setOutputDir(Provider outputDir) {
        this.outputDir.set(outputDir);
    }

    @TaskAction
    public void generate() {
        try {
            writeFile(new File(getOutputDir(), "index.html"), getProjectDescriptor().getName());
        } catch (IOException e) {
            throw new GradleException("Unable to generate site", e);
        }
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
