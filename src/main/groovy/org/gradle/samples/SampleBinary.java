package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Represents a sample tailored for a particular DSL.
 */
public interface SampleBinary extends Named {
    @Override
    String getName();

    /**
     * The sample for this binary.
     */
    Property<Sample> getSample();

    /**
     * The language this sample is written for
     */
    Property<Dsl> getDsl();

    /**
     * All content to include in the sample.
     */
    ConfigurableFileCollection getContent();

    /**
     * Content that is specific to the DSL language.
     */
    ConfigurableFileCollection getDslSpecificContent();

    /**
     * The documentation page for this sample
     */
    RegularFileProperty getSamplePageFile();

    /**
     * Working directory used by the plugin to validate or test this sample.
     */
    DirectoryProperty getWorkingDirectory();

    /**
     * Exclude patterns for files included in this sample
     */
    ListProperty<String> getExcludes();

    /**
     * A zip containing this sample.  This is the primary thing produced by a sample for a given language.
     */
    RegularFileProperty getZipFile();

    /**
     * A installation directory containing this sample.  This can be used to get an installed version of the sample.
     *
     * NOTE: This directory may be reused by other consumers.
     */
    DirectoryProperty getInstallDirectory();
}
