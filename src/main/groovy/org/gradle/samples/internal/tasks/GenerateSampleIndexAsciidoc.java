package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.samples.Sample;
import org.gradle.samples.SampleSummary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GenerateSampleIndexAsciidoc extends DefaultTask {
    @Nested
    public abstract ListProperty<SampleSummary> getSamples();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    private void doGenerate() throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(getOutputFile().get().getAsFile())) {
            out.println("= Sample Index");
            out.println();

            if (getSamples().get().isEmpty()) {
                out.println("No available samples.");
            } else {
                Map<String, List<SampleSummary>> categorizedSamples = new TreeMap<>();
                getSamples().get().forEach(sample -> {
                    String category = sample.getCategory().get();
                    List<SampleSummary> groupedSamples = categorizedSamples.computeIfAbsent(category, k -> new ArrayList<>());
                    groupedSamples.add(sample);
                });

                categorizedSamples.forEach((category, samples) -> {
                    Collections.sort(samples, Comparator.comparing(sample -> sample.getDisplayName().get()));
                    out.println("== " + category);
                    out.println();
                    samples.forEach(sample -> {
                        String description = sample.getDescription().get();
                        if (description.isEmpty()) {
                            out.println(String.format("- <<%s,%s>>", sample.getSampleDocName().get(), sample.getDisplayName().get()));
                        } else {
                            out.println(String.format("- <<%s,%s>>: %s", sample.getSampleDocName().get(), sample.getDisplayName().get(), description));
                        }
                    });
                });
            }
        }
    }
}
