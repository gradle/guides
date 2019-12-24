package org.gradle.docs.guides;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;

public interface Guides {
    /**
     * By convention, this is src/docs/guides
     *
     * @return The root guide directory.
     */
    DirectoryProperty getGuidesRoot();

    NamedDomainObjectContainer<? extends Guide> getPublishedGuides();
}
