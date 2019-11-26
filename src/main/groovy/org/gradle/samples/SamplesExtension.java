package org.gradle.samples;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.samples.internal.DefaultSample;
import org.gradle.samples.internal.DefaultSampleBinary;

import javax.inject.Inject;

public interface SamplesExtension {

    /**
     * The root sample directory.
     *
     * By convention, this is src/samples
     */
    DirectoryProperty getSamplesRoot();

    /**
     * The root install directory.
     *
     * By convention, this is buildDir/install/samples
     */
    DirectoryProperty getInstallRoot();

    /**
     * The root directory for all documentation.
     *
     * By convention, this is buildDir/samples/docs
     */
    DirectoryProperty getDocumentationRoot();

    /**
     * The generated samples index file.
     *
     * This is an asciidoc file, not the generated HTML.
     *
     * By convention, this is documentationRoot/index_samples.adoc
     */
    RegularFileProperty getSampleIndexFile();

    /**
     * All documentation for samples.
     *
     * This is not the generated HTML.
     */
    ConfigurableFileCollection getAssembledDocumentation();

    /**
     * List of common exclude patterns when building a zip or install directory for a sample.
     *
     * By convention, this excludes build and .gradle directories.
     */
    ListProperty<String> getCommonExcludes();

    /**
     * Container of all published samples. This is the primary configuration point for all samples.
     */
    NamedDomainObjectContainer<Sample> getPublishedSamples();

    /**
     * Container of all "sample binaries".  A sample binary is a sample for a particular DSL.
     *
     * Most configuration should happen in the "published samples" container above.
     */
    NamedDomainObjectContainer<SampleBinary> getBinaries();
}
