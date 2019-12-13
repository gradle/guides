package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.samples.Dsl;
import org.gradle.samples.SampleSummary;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

public abstract class GenerateSamplePageAsciidoc extends DefaultTask {
    @Nested
    public abstract Property<SampleSummary> getSampleSummary();

    @InputFile
    public abstract RegularFileProperty getReadmeFile();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() {
        // TODO: Add useful information to the readme
        File outputFile = getOutputFile().get().getAsFile();
        Path sourceFile = getReadmeFile().get().getAsFile().toPath();

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            bos.write(generateHeaderForSamplePage());
            Files.copy(sourceFile, bos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] generateHeaderForSamplePage() {
        SampleSummary sampleSummary = getSampleSummary().get();
        String sampleDisplayName = sampleSummary.getDisplayName().get();
        String sampleDocName = sampleSummary.getSampleDocName().get();

        StringBuilder sb = new StringBuilder();
        sb.append("= ").append(sampleDisplayName).append(" Sample\n\n");
        sb.append("[.download]\n");
        Set<Dsl> dsls = new TreeSet<>(sampleSummary.getDsls().get());
        for (Dsl dsl : dsls) {
            sb.append(String.format("- link:zips/%s-%s.zip[icon:download[] %s DSL]\n", sampleDocName, dsl.getDslLabel(), dsl.getDisplayName()));
        }
        sb.append('\n');
        return sb.toString().getBytes();
    }
}
