package org.gradle.plugins.site.data;

import org.gradle.api.GradleException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SiteGenerator {

    private final File outputDir;

    public SiteGenerator(File outputDir) {
        this.outputDir = outputDir;
    }

    public void generate(ProjectDescriptor projectDescriptor) {
        try {
            writeFile(new File(outputDir, "index.html"), projectDescriptor.getName());
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
