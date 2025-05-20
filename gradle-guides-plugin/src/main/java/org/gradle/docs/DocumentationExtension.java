package org.gradle.docs;

import org.gradle.api.Action;
import org.gradle.docs.guides.Guides;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.snippets.Snippets;

public interface DocumentationExtension {
    Guides getGuides();

    void guides(Action<? super Guides> action);

    Samples getSamples();

    void samples(Action<? super Samples> action);

    Snippets getSnippets();

    void snippets(Action<? super Snippets> action);
}
