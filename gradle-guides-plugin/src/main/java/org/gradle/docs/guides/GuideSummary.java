package org.gradle.docs.guides;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface GuideSummary {
    /**
     * @return Property for configuring the category this guide appears in.
     */
    Property<String> getCategory();

    /**
     * @return Property for configuring the guide documentation permalink.
     */
    Property<String> getPermalink();

    /**
     * @return Property for configuring the guide display name. The display name is used within the guide index.
     */
    Property<String> getDisplayName();
}
