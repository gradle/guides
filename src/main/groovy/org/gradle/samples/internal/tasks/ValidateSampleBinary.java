package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.samples.Dsl;
import org.gradle.samples.SampleBinary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public abstract class ValidateSampleBinary extends DefaultTask {
    // TODO: Maybe this isn't a good idea to pollute the SampleBinary with I/O annotations.
    @Nested
    public abstract Property<SampleBinary> getSampleBinary();

    @OutputFile
    public abstract RegularFileProperty getReportFile();

    @TaskAction
    public void validate() {
        // TODO: check for exemplar conf files?

        SampleBinary binary = getSampleBinary().get();

        Dsl dsl = binary.getDsl().get();
        String settingsFileName = getSettingsFileName(dsl);
        String readme = binary.getSamplePageFile().get().getAsFile().getName();
        List<String> requiredContents = Arrays.asList(readme, settingsFileName, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties");

        // Does the install directory look correct?
        File installDir = binary.getInstallDirectory().get().getAsFile();
        for (String required : requiredContents) {
            assertFileExists(binary, dsl, installDir, required);
        }

        // Does the Zip look correct?
        File zipFile = binary.getZipFile().get().getAsFile();
        try (ZipFile zip = new ZipFile(zipFile)) {
            for (String required : requiredContents) {
                assertZipContains(binary, dsl, zip, required);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: Would be nice to put the failures in this file too.
        File reportFile = getReportFile().get().getAsFile();
        try (PrintWriter fw = new PrintWriter(reportFile)) {
            fw.println(binary.getName() + " looks valid.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertZipContains(SampleBinary binary, Dsl dsl, ZipFile zip, String file) {
        ZipEntry entry = zip.getEntry(file);
        if (entry == null) {
            throw new GradleException("Sample '" + binary.getName() + "' for " + dsl.getDisplayName() + " DSL is invalid due to missing '" + file + "' file in ZIP.");
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

    private void assertFileExists(SampleBinary binary, Dsl dsl, File installDir, String fileName) {
        File file = new File(installDir, fileName);
        if (!file.exists()) {
            throw new GradleException("Sample '" + binary.getName() + "' for " + dsl.getDisplayName() + " DSL is invalid due to missing '" + fileName + "' file.");
        }
    }
}
