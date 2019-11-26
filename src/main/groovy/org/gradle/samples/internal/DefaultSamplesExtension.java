package org.gradle.samples.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.samples.Sample;
import org.gradle.samples.SampleBinary;
import org.gradle.samples.SamplesExtension;

import javax.inject.Inject;

public abstract class DefaultSamplesExtension implements SamplesExtension {
    private final NamedDomainObjectContainer<Sample> publishedSamples;
    private final NamedDomainObjectContainer<SampleBinary> binaries;

    @Inject
    public DefaultSamplesExtension(ObjectFactory objectFactory) {
        this.publishedSamples = objectFactory.domainObjectContainer(Sample.class, name -> objectFactory.newInstance(DefaultSample.class, name));
        this.binaries = objectFactory.domainObjectContainer(SampleBinary.class, name -> objectFactory.newInstance(DefaultSampleBinary.class, name));
    }

    @Override
    public NamedDomainObjectContainer<Sample> getPublishedSamples() {
        return publishedSamples;
    }

    @Override
    public NamedDomainObjectContainer<SampleBinary> getBinaries() {
        return binaries;
    }
}
