package org.gradle.samples.internal;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.samples.Sample;

import javax.inject.Inject;

public abstract class DefaultSample implements Sample {
    private final String name;
    private final DomainObjectSet<DslSampleArchive> archives;

    @Inject
    public DefaultSample(String name, ObjectFactory objectFactory) {
        this.name = name;
        archives = objectFactory.domainObjectSet(DslSampleArchive.class);
    }

    @Override
    public abstract DirectoryProperty getSampleDir();

    // Implementation notes: At some point this may have to support "need at least version X" (Guides) and "for version X" (Gradle).
    @Override
    public abstract Property<String> getGradleVersion();

    public DomainObjectSet<DslSampleArchive> getDslSampleArchives() {
        return archives;
    }

    @Override
    public String getName() {
        return name;
    }
}
