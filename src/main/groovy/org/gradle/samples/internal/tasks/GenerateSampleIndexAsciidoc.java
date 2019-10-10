package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public abstract class GenerateSampleIndexAsciidoc extends DefaultTask {
    @Inject
    public GenerateSampleIndexAsciidoc() {
        onlyIf(this::hasSamplesToIndex);
    }

    private boolean hasSamplesToIndex(Task task) {
        return !getSampleInformation().get().isEmpty();
    }

    @Nested
    public abstract ListProperty<SampleInformation> getSampleInformation();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    private void doGenerate() throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(getOutputFile().get().getAsFile())) {
            out.println("= Sample Index");
            out.println();
            getSampleInformation().get().forEach(sampleInformation -> {
                out.print("- link:" + sampleInformation.getPath() + "/[Sample " + sampleInformation.getName() + "]");
                if (sampleInformation.getDescription() == null || sampleInformation.getDescription().isEmpty()) {
                    out.println();
                } else {
                    out.println(": " + sampleInformation.getDescription());
                }
            });
        }
    }

    public static class SampleInformation {
        private final String path;
        private final String description;

        public SampleInformation(String path, @Nullable String description) {
            this.path = path;
            this.description = description;
        }

        public String getName() {
            return path;
        }

        @Input
        public String getPath() {
            return path;
        }

        @Input
        @Optional
        @Nullable
        public String getDescription() {
            return description;
        }
    }
}
