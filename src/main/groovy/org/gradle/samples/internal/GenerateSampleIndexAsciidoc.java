package org.gradle.samples.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public abstract class GenerateSampleIndexAsciidoc extends DefaultTask {
    @Input
    public abstract ListProperty<String> getSamplePaths();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    private void doGenerate() throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(getOutputFile().get().getAsFile())) {
            out.println("= Sample Index");
            out.println();
            getSamplePaths().get().forEach(path -> {
                out.println("- link:" + path + "/[Sample " + path + "]");
            });
        }
    }
}
