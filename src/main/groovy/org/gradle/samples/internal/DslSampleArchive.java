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

    DslSampleArchive(String name, String languageName, String classifier) {
        this.name = name;
        this.languageName = languageName;
        this.classifier = classifier;
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

    String getSyncTaskName() {
        return "sync" + capitalize(name) + "Sample";
    }

    String getCompressTaskName() {
        return "compress" + capitalize(name) + "Sample";
    }

    abstract DirectoryProperty getAssembleDirectory();

    abstract DirectoryProperty getArchiveDirectory();

    abstract RegularFileProperty getArchiveFile();

    DslSampleArchive configureFrom(DefaultSample sample) {
        getArchiveContent().from(sample.getArchiveContent());
        getArchiveContent().from(sample.getSampleDir().dir(getLanguageName()));
        getArchiveContent().from(sample.getReadMeFile());
        getAssembleDirectory().set(sample.getIntermediateDirectory().dir(getClassifier() + "-content"));
        getArchiveDirectory().set(sample.getIntermediateDirectory().dir(getClassifier() + "-zip"));
        getArchiveFile().set(sample.getIntermediateDirectory().file(sample.getArchiveBaseName().map(baseName -> getClassifier() + "-zip/" + baseName + "-" + getClassifier() + ".zip")));
        return this;
    }
}
