package org.gradle.samples;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;

/**
 * A custom extension for configuring the Exemplar configuration for testing the samples.
 */
public interface ExemplarExtension {
    /**
     * Property for configuring the exemplar source for testing.
     *
     * NOTE: It must include all the files required for exemplar to run it's tests.
     *
     * @return a file collection for configuring exemplar sources
     */
    ConfigurableFileCollection getSource();

    /**
     * Allow configuration of the install task when assembling all the exemplar sources.
     *
     * @param action the configuration action for the install task
     */
    void source(Action<? super CopySpec> action);
}
