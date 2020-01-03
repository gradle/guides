package org.gradle.docs.guides.internal;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.guides.Guides;
import org.gradle.docs.guides.GuidesDistribution;

import javax.inject.Inject;

public abstract class GuidesInternal implements Guides {
    private final NamedDomainObjectContainer<GuideInternal> publishedGuides;
    private final DomainObjectSet<GuideBinary> binaries;
    private final GuidesDistribution guidesDistribution;

    @Inject
    public GuidesInternal(ObjectFactory objectFactory) {
        this.publishedGuides = objectFactory.domainObjectContainer(GuideInternal.class, name -> objectFactory.newInstance(GuideInternal.class, name));
        this.binaries = objectFactory.domainObjectSet(GuideBinary.class);
        this.guidesDistribution = objectFactory.newInstance(GuidesDistribution.class);
    }

    @Override
    public NamedDomainObjectContainer<? extends GuideInternal> getPublishedGuides() {
        return publishedGuides;
    }

    public DomainObjectSet<? super GuideBinary> getBinaries() {
        return binaries;
    }

    @Override
    public GuidesDistribution getDistribution() {
        return guidesDistribution;
    }

    /**
     * By convention, this is buildDir/working/guides/docs
     *
     * @return The root directory for all documentation.
     */
    public abstract DirectoryProperty getDocumentationInstallRoot();

    /**
     * By convention, this is buildDir/working/guides/render-samples
     *
     * @return The root directory for rendered documentation.
     */
    public abstract DirectoryProperty getRenderedDocumentationRoot();
}
