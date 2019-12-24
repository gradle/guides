package org.gradle.docs.samples.internal;

import org.gradle.docs.samples.SampleBinary;

import javax.inject.Inject;

public abstract class DefaultSampleBinary implements SampleBinary {
    private final String name;

    @Inject
    public DefaultSampleBinary(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
