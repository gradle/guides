package org.gradle.docs.samples;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;

public interface Samples {

    /**
     * By convention, this is src/samples
     *
     * @return The root sample directory.
     */
    DirectoryProperty getSamplesRoot();

    /**
     * By convention, this is src/samples/templates
     *
     * @return The root template directory.
     */
    DirectoryProperty getTemplatesRoot();

    /**
     * This is an asciidoc file, not the generated HTML.
     *
     * By convention, this is documentationRoot/index.adoc
     *
     * @return The generated samples index file.
     */
    RegularFileProperty getSampleIndexFile();

    /**
     * @return Buildable elements of all the available samples.
     */
    SamplesDistribution getDistribution();

    /**
     * @return Container of all published samples. This is the primary configuration point for all samples.
     */
    NamedDomainObjectContainer<? extends Sample> getPublishedSamples();

    /**
     * @return Container of all templates.  Templates are reusable parts of a sample.
     */
    NamedDomainObjectContainer<? extends Template> getTemplates();
}
