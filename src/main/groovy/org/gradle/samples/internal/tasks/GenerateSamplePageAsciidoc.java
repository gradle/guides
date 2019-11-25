package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class GenerateSamplePageAsciidoc extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getSourceFile();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() {
        // TODO: Generate properly
        // TODO: Process the README file to add links to the archives

        getProject().sync(copySpec -> {
            copySpec.from(getSourceFile());
            File outputFile = getOutputFile().getAsFile().get();
            copySpec.into(outputFile.getParentFile());
            copySpec.rename(original -> outputFile.getName());
        });
    }
}
