package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class GenerateSanityCheckTests extends DefaultTask {
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws IOException {
        File outputFile = getOutputFile().get().getAsFile();
        StringBuilder sb = new StringBuilder();
        sb.append("commands: [{\n");
        sb.append("    executable: gradle\n");
        sb.append("    args: help -q\n");
        sb.append("}]\n");
        try (FileWriter fw = new FileWriter(outputFile)) {
            fw.write(sb.toString());
        }
    }
}
