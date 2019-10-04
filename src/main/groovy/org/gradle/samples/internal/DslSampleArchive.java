package org.gradle.samples.internal;

import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;

import javax.inject.Inject;

public abstract class DslSampleArchive implements Named {
    private final String name;

    @Inject
    public DslSampleArchive(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract ConfigurableFileCollection getArchiveContent();
}
