package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.samples.Sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public abstract class GenerateSampleIndexAsciidoc extends DefaultTask {
    @Nested
    public abstract ListProperty<Sample> getSamples();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    private void doGenerate() throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(getOutputFile().get().getAsFile())) {
            out.println("= Sample Index");
            out.println();

            List<Sample> samples = getSamples().get();

            if (samples.isEmpty()) {
                out.println("No available samples.");
            } else {
                samples.forEach(sample -> {
                    File samplePage = sample.getSamplePageFile().get().getAsFile();
                    String description = sample.getDescription().get();
                    if (description.isEmpty()) {
                        out.println(String.format("- <<%s,%s>>", samplePage.getName(), sample.getDisplayName().get()));
                    } else {
                        out.println(String.format("- <<%s,%s>>: %s", samplePage.getName(), sample.getDisplayName().get(), description));
                    }
                });
            }
        }
    }
}
