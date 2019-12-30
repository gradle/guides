package org.gradle.docs;

import org.gradle.api.Action;
import org.gradle.docs.guides.Guides;
import org.gradle.docs.samples.Samples;

public interface DocumentationExtension {
    Guides getGuides();

    void guides(Action<? super Guides> action);

    Samples getSamples();

    void samples(Action<? super Samples> action);
}
