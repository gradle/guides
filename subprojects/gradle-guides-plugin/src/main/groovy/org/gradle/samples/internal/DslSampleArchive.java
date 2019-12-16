package org.gradle.samples.internal;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.samples.DomainSpecificSample;

import static org.gradle.samples.internal.StringUtils.capitalize;

public abstract class DslSampleArchive implements Named, DomainSpecificSample {
    private final String name;
    private final String languageName;
    private final String classifier;
    private final String settingsFileName;

    DslSampleArchive(String name, String languageName, String classifier, String settingsFileName) {
        this.name = name;
        this.languageName = languageName;
        this.classifier = classifier;
        this.settingsFileName = settingsFileName;
    }

    @Override
    public String getName() {
        return name;
    }

    String getLanguageName() {
        return languageName;
    }

    String getClassifier() {
        return classifier;
    }

    String getInstallTaskName() {
        return "install" + capitalize(name) + "Sample";
    }

    String getCompressTaskName() {
        return "compress" + capitalize(name) + "Sample";
    }

    abstract DirectoryProperty getInstallDirectory();

    abstract DirectoryProperty getArchiveDirectory();

    abstract RegularFileProperty getArchiveFile();

    String getSettingsFileName() {
        return settingsFileName;
    }

    DslSampleArchive configureFrom(DefaultSample sample) {
        getArchiveContent().from(sample.getSampleDirectory().dir(getLanguageName()));
        return configureDefaults(sample);
    }

    DslSampleArchive configureDefaults(DefaultSample sample) {
        getArchiveContent().from(sample.getArchiveContent());
        getArchiveContent().from(sample.getReadMeFile());
        getInstallDirectory().set(sample.getIntermediateDirectory().dir(getClassifier() + "-content"));
        getArchiveDirectory().set(sample.getIntermediateDirectory().dir(getClassifier() + "-zip"));
        getArchiveFile().set(sample.getIntermediateDirectory().file(sample.getArchiveBaseName().map(baseName -> getClassifier() + "-zip/" + baseName + "-" + getClassifier() + ".zip")));
        return this;
    }
}
