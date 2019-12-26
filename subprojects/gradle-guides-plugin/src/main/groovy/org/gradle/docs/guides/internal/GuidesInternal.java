package org.gradle.docs.guides.internal;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.guides.Guide;
import org.gradle.docs.guides.Guides;

import javax.inject.Inject;

public abstract class GuidesInternal implements Guides {
    private final NamedDomainObjectContainer<GuideInternal> publishedGuides;

    @Inject
    public GuidesInternal(ObjectFactory objectFactory) {
        this.publishedGuides = objectFactory.domainObjectContainer(GuideInternal.class, name -> objectFactory.newInstance(GuideInternal.class, name));
    }

    @Override
    public NamedDomainObjectContainer<? extends Guide> getPublishedGuides() {
        return publishedGuides;
    }
}
