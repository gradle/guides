package org.gradle.samples;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.samples.internal.DefaultSample;
import org.gradle.samples.internal.DefaultSampleBinary;

import javax.inject.Inject;

public abstract class SamplesExtension {
    private final NamedDomainObjectContainer<Sample> publishedSamples;
    private final NamedDomainObjectContainer<SampleBinary> binaries;

    @Inject
    public SamplesExtension(ObjectFactory objectFactory) {
        this.publishedSamples = objectFactory.domainObjectContainer(Sample.class, name -> objectFactory.newInstance(DefaultSample.class, name));
        this.binaries = objectFactory.domainObjectContainer(SampleBinary.class, name -> objectFactory.newInstance(DefaultSampleBinary.class, name));
    }

    public abstract DirectoryProperty getSamplesRoot();
    public abstract DirectoryProperty getInstallRoot();
    public abstract RegularFileProperty getSampleIndexFile();
    public abstract DirectoryProperty getDocumentationRoot();
    public abstract ConfigurableFileCollection getAssembledDocumentation();
    public abstract ListProperty<String> getCommonExcludes();

    public NamedDomainObjectContainer<Sample> getPublishedSamples() {
        return publishedSamples;
    }

    public NamedDomainObjectContainer<SampleBinary> getBinaries() {
        return binaries;
    }
}
