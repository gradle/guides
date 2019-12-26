package org.gradle.docs;

import org.gradle.api.Action;
import org.gradle.docs.guides.Guides;

public interface DocumentationExtension {
    Guides getGuides();

    void guides(Action<? super Guides> action);
}
