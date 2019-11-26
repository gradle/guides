package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class GenerateSamplePageAsciidoc extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getReadmeFile();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() {
        // TODO: Process the README file to add links to the archives
        // TODO: Add useful information to the readme

        getProject().sync(copySpec -> {
            copySpec.from(getReadmeFile());

            File outputFile = getOutputFile().getAsFile().get();
            copySpec.into(outputFile.getParentFile());
            copySpec.rename(original -> outputFile.getName());
        });
    }
}
