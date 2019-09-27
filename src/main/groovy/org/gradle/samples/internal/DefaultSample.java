package org.gradle.samples.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.samples.Sample;

import javax.inject.Inject;

public abstract class DefaultSample implements Sample {
    private final String name;

    @Inject
    public DefaultSample(String name) {
        this.name = name;
    }

    @Override
    public abstract DirectoryProperty getSampleDir();

    @Override
    public String getName() {
        return name;
    }
}
