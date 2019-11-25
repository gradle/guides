package org.gradle.samples.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.samples.Dsl;
import org.gradle.samples.SampleBinary;

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

    public abstract Property<Dsl> getDsl();
    public abstract ConfigurableFileCollection getContent();
    public abstract DirectoryProperty getStagingDirectory();
    public abstract RegularFileProperty getZipFile();
    public abstract DirectoryProperty getInstallDirectory();
    public abstract RegularFileProperty getSamplePageFile();
}
