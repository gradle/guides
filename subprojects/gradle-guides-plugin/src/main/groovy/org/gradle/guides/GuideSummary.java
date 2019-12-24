package org.gradle.guides;

import org.gradle.api.provider.Property;

public interface GuideSummary {
    /**
     * @return Property for configuring the category this guide appears in.
     */
    Property<String> getCategory();
}
