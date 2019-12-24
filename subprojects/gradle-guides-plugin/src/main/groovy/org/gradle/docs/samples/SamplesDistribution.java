package org.gradle.docs.samples;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;

/**
 * This represents the outputs of the samples section of the documentation.
 */
public abstract class SamplesDistribution {

    /**
     * @return the collection of samples extracted as a consumer would see them
     */
    public abstract ConfigurableFileTree getInstalledSamples();

    /**
     * @return a collection of samples in zip form
     */
    public abstract ConfigurableFileCollection getZippedSamples();

    /**
     * @return Collection of samples ready for testing
     */
    public abstract ConfigurableFileTree getTestedInstalledSamples();

    /**
     * This is HTML and resources.
     *
     * @return collection of rendered documentation
     */
    public abstract ConfigurableFileCollection getRenderedDocumentation();
}
