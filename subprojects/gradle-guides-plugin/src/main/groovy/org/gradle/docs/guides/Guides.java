package org.gradle.docs.guides;

import org.gradle.api.NamedDomainObjectContainer;

public interface Guides {
    NamedDomainObjectContainer<? extends Guide> getPublishedGuides();
}
