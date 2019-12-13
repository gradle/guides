package org.gradle.samples.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.samples.Sample;
import org.gradle.samples.SampleBinary;
import org.gradle.samples.SampleSummary;
import org.gradle.samples.SamplesExtension;
import org.gradle.samples.Template;

import javax.inject.Inject;

public abstract class DefaultSamplesExtension implements SamplesExtension {
    private final NamedDomainObjectContainer<Sample> publishedSamples;
    private final NamedDomainObjectContainer<SampleBinary> binaries;
    private final NamedDomainObjectContainer<Template> templates;

    @Inject
    public DefaultSamplesExtension(ObjectFactory objectFactory) {
        this.publishedSamples = objectFactory.domainObjectContainer(Sample.class, name -> objectFactory.newInstance(DefaultSample.class, name));
        this.binaries = objectFactory.domainObjectContainer(SampleBinary.class, name -> objectFactory.newInstance(DefaultSampleBinary.class, name));
        this.templates = objectFactory.domainObjectContainer(Template.class, name -> objectFactory.newInstance(DefaultTemplate.class, name));
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
}
