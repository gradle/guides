package org.gradle.samples;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface SampleSummary {
    /**
     * @return Property for configuring the sample description. The description is used within the sample index.
     */
    @Input
    Property<String> getDescription();

    /**
     * @return Property for configuring the sample display name. The display name is used within the sample index.
     */
    @Input
    Property<String> getDisplayName();

    /**
     * @return Property for configuring the sample documentation name. This is the name other parts of the documentation would use to refer to the sample.
     */
    @Input
    Property<String> getSampleDocName();

    /**
     * @return Property for configuring the category this sample appears in.
     */
    @Input
    Property<String> getCategory();
}
