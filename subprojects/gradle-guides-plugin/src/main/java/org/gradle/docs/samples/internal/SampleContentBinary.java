package org.gradle.docs.samples.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.docs.samples.SampleSummary;

import javax.inject.Inject;

public abstract class SampleContentBinary extends SampleBinary {
    @Inject
    public SampleContentBinary(String name) {
        super(name);
    }

    public abstract Property<String> getDisplayName();

    public abstract DirectoryProperty getSampleDirectory();

    public abstract Property<String> getPermalink();

    public abstract RegularFileProperty getIndexPageFile();

    public abstract Property<SampleSummary> getSummary();
}
