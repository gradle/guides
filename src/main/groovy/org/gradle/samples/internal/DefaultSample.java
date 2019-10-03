package org.gradle.samples.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
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

    // Implementation notes: At some point this may have to support "need at least version X" (Guides) and "for version X" (Gradle).
    @Override
    public abstract Property<String> getGradleVersion();

    @Override
    public String getName() {
        return name;
    }
}
