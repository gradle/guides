package org.gradle.samples;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;

public interface SamplesExtension {

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
     * By convention, this is buildDir/working/samples/install
     *
     * @return The root install directory.
     */
    DirectoryProperty getInstallRoot();

    /**
     * By convention, this is buildDir/working/samples/testing
     *
     * @return location of installed samples ready for testing
     */
    DirectoryProperty getTestedInstallRoot();

    /**
     * By convention, this is buildDir/working/samples/docs
     *
     * @return The root directory for all documentation.
     */
    DirectoryProperty getDocumentationInstallRoot();

    /**
     * By convention, this is buildDir/working/samples/render-samples
     *
     * @return The root directory for rendered documentation.
     */
    DirectoryProperty getRenderedDocumentationRoot();

    /**
     * By convention, this excludes build and .gradle directories.
     *
     * @return List of common exclude patterns when building a zip or install directory for a sample.
     */
    ListProperty<String> getCommonExcludes();

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
    NamedDomainObjectContainer<Sample> getPublishedSamples();

    /**
     * @return Container of all templates.  Templates are reusable parts of a sample.
     */
    NamedDomainObjectContainer<Template> getTemplates();

    /**
     * @return Container of all "sample binaries".  A sample binary is a sample for a particular DSL.
     *
     * Most configuration should happen in the "published samples" container.
     */
    NamedDomainObjectContainer<SampleBinary> getBinaries();
}
