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

    /**
     * @return Buildable elements of all the available guides.
     */
    GuidesDistribution getDistribution();

    /**
     * @return Container of all published guides. This is the primary configuration point for all guides.
     */
    NamedDomainObjectContainer<? extends Guide> getPublishedGuides();
}
