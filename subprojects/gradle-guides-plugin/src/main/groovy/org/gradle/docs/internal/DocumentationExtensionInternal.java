package org.gradle.docs.internal;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.DocumentationExtension;
import org.gradle.docs.guides.Guides;
import org.gradle.docs.guides.internal.GuidesInternal;

import javax.inject.Inject;

public abstract class DocumentationExtensionInternal implements DocumentationExtension {
    private final GuidesInternal guides;

    @Inject
    public DocumentationExtensionInternal(ObjectFactory objectFactory) {
        this.guides = objectFactory.newInstance(GuidesInternal.class);
    }

    @Override
    public GuidesInternal getGuides() {
        return guides;
    }

    @Override
    public void guides(Action<? super Guides> action) {
        action.execute(guides);
    }
}
