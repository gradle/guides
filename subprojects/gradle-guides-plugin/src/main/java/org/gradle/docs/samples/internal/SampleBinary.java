package org.gradle.docs.samples.internal;

import org.gradle.api.Named;

import javax.inject.Inject;

public abstract class SampleBinary implements Named {
    private final String name;

    @Inject
    public SampleBinary(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
