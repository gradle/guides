package org.gradle.samples;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;

public interface SamplesExtension {

    /**
     * @return The root sample directory.
     *
     * By convention, this is src/samples
     */
    DirectoryProperty getSamplesRoot();

    /**
     * @return The root template directory.
     *
     * By convention, this is src/samples/templates
     */
    DirectoryProperty getTemplatesRoot();

    /**
     * @return The root install directory.
     *
     * By convention, this is buildDir/install/samples
     */
    DirectoryProperty getInstallRoot();
    ConfigurableFileTree getInstalledSamples();
    ConfigurableFileCollection getZippedSamples();

    DirectoryProperty getTestedInstallRoot();
    ConfigurableFileTree getTestedInstalledSamples();

    /**
     * @return The root directory for all documentation.
     *
     * By convention, this is buildDir/samples/docs
     */
    DirectoryProperty getDocumentationRoot();

    DirectoryProperty getRenderedDocumentationRoot();
    ConfigurableFileCollection getRenderedDocumentation();

    /**
     * @return The generated samples index file.
     *
     * This is an asciidoc file, not the generated HTML.
     *
     * By convention, this is documentationRoot/index.adoc
     */
    RegularFileProperty getSampleIndexFile();

    /**
     * @return All documentation for samples.
     *
     * This is not the generated HTML.
     */
    ConfigurableFileCollection getAssembledDocumentation();

    /**
     * @return List of common exclude patterns when building a zip or install directory for a sample.
     *
     * By convention, this excludes build and .gradle directories.
     */
    ListProperty<String> getCommonExcludes();

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
