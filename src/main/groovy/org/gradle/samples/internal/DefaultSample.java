package org.gradle.samples.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.samples.Sample;

import javax.inject.Inject;

public abstract class DefaultSample implements Sample {
    private final String name;
    private final Property<String> gradleVersion;

    @Inject
    public DefaultSample(String name, ObjectFactory objectFactory) {
        this.name = name;
        this.gradleVersion = objectFactory.property(String.class);
    }

    @Override
    public abstract DirectoryProperty getSampleDir();

    // Implementation notes: At some point this may have to support "need at least version X" (Guides) and "for version X" (Gradle).
    @Override
    public Property<String> getGradleVersion() {
        return gradleVersion;
    }

    @Override
    public String getName() {
        return name;
    }
}
