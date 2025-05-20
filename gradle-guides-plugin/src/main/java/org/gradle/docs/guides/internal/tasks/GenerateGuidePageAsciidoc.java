package org.gradle.docs.guides.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public abstract class GenerateGuidePageAsciidoc extends DefaultTask {
    @Input
    public abstract MapProperty<String, String> getAttributes();

    @InputFile
    public abstract RegularFileProperty getIndexFile();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() {
        File outputFile = getOutputFile().get().getAsFile();
        Path sourceFile = getIndexFile().get().getAsFile().toPath();

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            bos.write(generateHeaderForGuidePage());
            Files.copy(sourceFile, bos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] generateHeaderForGuidePage() {
        StringBuilder sb = new StringBuilder();
        writeAttributes(sb);
        return sb.toString().getBytes();
    }

    private void writeAttributes(StringBuilder sb) {
        Map<String, String> attributes = getAttributes().get();

        attributes.entrySet().forEach(it -> {
            sb.append(":").append(it.getKey()).append(": ").append(it.getValue()).append("\n");
        });
        sb.append('\n');
    }
}
