package org.gradle.docs.samples.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.samples.Sample;
import org.gradle.docs.samples.SampleBinary;
import org.gradle.docs.samples.SamplesDistribution;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.Template;

import javax.inject.Inject;

public abstract class SamplesInternal implements Samples {
    private final NamedDomainObjectContainer<SampleInternal> publishedSamples;
    private final NamedDomainObjectContainer<SampleBinary> binaries;
    private final NamedDomainObjectContainer<TemplateInternal> templates;
    private final SamplesDistribution distribution;

    @Inject
    public SamplesInternal(ObjectFactory objectFactory) {
        this.publishedSamples = objectFactory.domainObjectContainer(SampleInternal.class, name -> objectFactory.newInstance(SampleInternal.class, name));
        this.binaries = objectFactory.domainObjectContainer(SampleBinary.class, name -> objectFactory.newInstance(SampleBinaryInternal.class, name));
        this.templates = objectFactory.domainObjectContainer(TemplateInternal.class, name -> objectFactory.newInstance(TemplateInternal.class, name));
        this.distribution = objectFactory.newInstance(SamplesDistribution.class);
    }

    @Override
    public NamedDomainObjectContainer<? extends SampleInternal> getPublishedSamples() {
        return publishedSamples;
    }

    @Override
    public NamedDomainObjectContainer<SampleBinary> getBinaries() {
        return binaries;
    }

    @Override
    public NamedDomainObjectContainer<? extends TemplateInternal> getTemplates() {
        return templates;
    }

    @Override
    public SamplesDistribution getDistribution() {
        return distribution;
    }
}
