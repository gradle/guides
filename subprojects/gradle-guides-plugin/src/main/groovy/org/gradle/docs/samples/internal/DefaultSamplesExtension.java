package org.gradle.docs.samples.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.samples.Sample;
import org.gradle.docs.samples.SampleBinary;
import org.gradle.docs.samples.SamplesDistribution;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.Template;

import javax.inject.Inject;

public abstract class DefaultSamplesExtension implements Samples {
    private final NamedDomainObjectContainer<Sample> publishedSamples;
    private final NamedDomainObjectContainer<SampleBinary> binaries;
    private final NamedDomainObjectContainer<Template> templates;
    private final SamplesDistribution distribution;

    @Inject
    public DefaultSamplesExtension(ObjectFactory objectFactory) {
        this.publishedSamples = objectFactory.domainObjectContainer(Sample.class, name -> objectFactory.newInstance(DefaultSample.class, name));
        this.binaries = objectFactory.domainObjectContainer(SampleBinary.class, name -> objectFactory.newInstance(DefaultSampleBinary.class, name));
        this.templates = objectFactory.domainObjectContainer(Template.class, name -> objectFactory.newInstance(DefaultTemplate.class, name));
        this.distribution = objectFactory.newInstance(SamplesDistribution.class);
    }

    @Override
    public NamedDomainObjectContainer<Sample> getPublishedSamples() {
        return publishedSamples;
    }

    @Override
    public NamedDomainObjectContainer<SampleBinary> getBinaries() {
        return binaries;
    }

    @Override
    public NamedDomainObjectContainer<Template> getTemplates() {
        return templates;
    }

    @Override
    public SamplesDistribution getDistribution() {
        return distribution;
    }
}
