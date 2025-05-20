package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.docs.samples.Dsl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ValidateSampleBinary extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getZipFile();

    @Input
    public abstract Property<Dsl> getDsl();

    @Internal
    public abstract Property<String> getSampleName();

    @OutputFile
    public abstract RegularFileProperty getReportFile();

    @TaskAction
    public void validate() {
        // TODO: check for exemplar conf files
        Dsl dsl = getDsl().get();
        String settingsFileName = getSettingsFileName(dsl);
        String name = getSampleName().get();
        List<String> requiredContents = Arrays.asList("README", settingsFileName, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties");

        // Does the Zip look correct?
        File zipFile = getZipFile().get().getAsFile();
        try (ZipFile zip = new ZipFile(zipFile)) {
            for (String required : requiredContents) {
                assertZipContains(name, dsl, zip, required);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: Would be nice to put the failures in this file too.
        File reportFile = getReportFile().get().getAsFile();
        try (PrintWriter fw = new PrintWriter(reportFile)) {
            fw.println(name + " looks valid.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertZipContains(String name, Dsl dsl, ZipFile zip, String file) {
        ZipEntry entry = zip.getEntry(file);
        if (entry == null) {
            throw new GradleException("Sample '" + name + "' for " + dsl.getDisplayName() + " DSL is invalid due to missing '" + file + "' file.");
        }
    }

    private String getSettingsFileName(Dsl dsl) {
        String settingsFileName;
        switch (dsl) {
            case GROOVY:
                settingsFileName = "settings.gradle";
                break;
            case KOTLIN:
                settingsFileName = "settings.gradle.kts";
                break;
            default:
                throw new GradleException("Unsupported DSL type");
        }
        return settingsFileName;
    }
}
