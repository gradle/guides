package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public abstract class GenerateSanityCheckTests extends DefaultTask {
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws IOException {
        File outputFile = getOutputFile().get().getAsFile();
        StringBuilder sb = new StringBuilder();
        sb.append("commands: [{");
        sb.append("\texecutable: gradle");
        sb.append("\targs: help -q");
        sb.append("}]");
        Files.writeString(outputFile.toPath(), sb.toString(), StandardOpenOption.TRUNCATE_EXISTING);
    }
}
