package org.gradle.samples.internal;

import org.gradle.api.Named;
import org.gradle.samples.DomainSpecificSample;
import org.gradle.samples.Sample;

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

    public String getLanguageName() {
        return languageName;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getSyncTaskName() {
        return "sync" + capitalize(name) + "Sample";
    }

    public String getCompressTaskName() {
        return "compress" + capitalize(name) + "Sample";
    }

    private static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }

    public DslSampleArchive configureFrom(Sample sample) {
        getArchiveContent().from(sample.getArchiveContent());
        getArchiveContent().from(sample.getSampleDir().dir(getLanguageName()));
        return this;
    }
}
