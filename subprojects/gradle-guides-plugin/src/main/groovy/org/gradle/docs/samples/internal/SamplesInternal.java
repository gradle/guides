package org.gradle.docs.samples.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.samples.SamplesDistribution;
import org.gradle.docs.samples.Samples;

import javax.inject.Inject;

public abstract class SamplesInternal implements Samples {
    private final NamedDomainObjectContainer<SampleInternal> publishedSamples;
    private final NamedDomainObjectContainer<SampleBinary> binaries;
    private final NamedDomainObjectContainer<TemplateInternal> templates;
    private final SamplesDistribution distribution;

    @Inject
    public SamplesInternal(ObjectFactory objectFactory) {
        this.publishedSamples = objectFactory.domainObjectContainer(SampleInternal.class, name -> objectFactory.newInstance(SampleInternal.class, name));
        this.binaries = objectFactory.domainObjectContainer(SampleBinary.class, name -> objectFactory.newInstance(SampleBinary.class, name));
        this.templates = objectFactory.domainObjectContainer(TemplateInternal.class, name -> objectFactory.newInstance(TemplateInternal.class, name));
        this.distribution = objectFactory.newInstance(SamplesDistribution.class);
    }

    @Override
    public NamedDomainObjectContainer<? extends SampleInternal> getPublishedSamples() {
        return publishedSamples;
    }

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

    /**
     * By convention, this is buildDir/working/samples/install
     *
     * @return The root install directory.
     */
    public abstract DirectoryProperty getInstallRoot();

    /**
     * By convention, this is buildDir/working/samples/testing
     *
     * @return location of installed samples ready for testing
     */
    public abstract DirectoryProperty getTestedInstallRoot();

    /**
     * By convention, this is buildDir/working/samples/docs
     *
     * @return The root directory for all documentation.
     */
    public abstract DirectoryProperty getDocumentationInstallRoot();

    /**
     * By convention, this is buildDir/working/samples/render-samples
     *
     * @return The root directory for rendered documentation.
     */
    public abstract DirectoryProperty getRenderedDocumentationRoot();
}
