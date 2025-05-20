package org.gradle.docs.internal;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.DocumentationExtension;
import org.gradle.docs.guides.Guides;
import org.gradle.docs.guides.internal.GuidesInternal;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.internal.SamplesInternal;
import org.gradle.docs.snippets.Snippets;
import org.gradle.docs.snippets.internal.SnippetsInternal;

import javax.inject.Inject;

public abstract class DocumentationExtensionInternal implements DocumentationExtension {
    private final GuidesInternal guides;
    private final SamplesInternal samples;
    private final SnippetsInternal snippets;

    @Inject
    public DocumentationExtensionInternal(ObjectFactory objectFactory) {
        this.guides = objectFactory.newInstance(GuidesInternal.class);
        this.samples = objectFactory.newInstance(SamplesInternal.class);
        this.snippets = objectFactory.newInstance(SnippetsInternal.class);
    }

    @Override
    public GuidesInternal getGuides() {
        return guides;
    }

    @Override
    public void guides(Action<? super Guides> action) {
        action.execute(guides);
    }

    @Override
    public SamplesInternal getSamples() {
        return samples;
    }

    @Override
    public void samples(Action<? super Samples> action) {
        action.execute(samples);
    }

    @Override
    public SnippetsInternal getSnippets() {
        return snippets;
    }

    @Override
    public void snippets(Action<? super Snippets> action) {
        action.execute(snippets);
    }
}
