package org.gradle.samples.internal;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.samples.Sample;

import javax.inject.Inject;

import static org.gradle.samples.internal.StringUtils.capitalize;

public abstract class DefaultSample implements Sample {
    private final String name;
    private final DomainObjectSet<DslSampleArchive> archives;
    private final ObjectFactory objectFactory;

    @Inject
    public DefaultSample(String name, ObjectFactory objectFactory) {
        this.name = name;
        archives = objectFactory.domainObjectSet(DslSampleArchive.class);
        this.objectFactory = objectFactory;
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

    Provider<RegularFile> getReadMeFile() {
        return getSampleDir().file("README.adoc");
    }

    abstract DirectoryProperty getIntermediateDirectory();

    abstract Property<String> getArchiveBaseName();

    abstract DirectoryProperty getOutputDirectory();

    abstract ConfigurableFileCollection getSource();

    @Override
    public void withGroovyDsl() {
        getDslSampleArchives().add(objectFactory.newInstance(GroovyDslSampleArchive.class, name).configureFrom(this));
    }

    @Override
    public void withKotlinDsl() {
        getDslSampleArchives().add(objectFactory.newInstance(KotlinDslSampleArchive.class, name).configureFrom(this));
    }

    String getWrapperTaskName() {
        return "generateWrapperFor" + capitalize(name) + "Sample";
    }

    DomainObjectSet<DslSampleArchive> getDslSampleArchives() {
        return archives;
    }
}
