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
    /**
     * @return The language this sample is written for
     */
    Property<Dsl> getDsl();

    /**
     * @return All content to include in the sample.
     */
    ConfigurableFileCollection getContent();

    /**
     * @return Content that is specific to the DSL language.
     */
    ConfigurableFileCollection getDslSpecificContent();

    ConfigurableFileCollection getTestsContent();

    /**
     * @return The documentation page for this sample
     */
    Property<String> getSampleLinkName();

    /**
     * @return Working directory used by the plugin to expose an assembled sample to consumers.
     */
    DirectoryProperty getWorkingDirectory();

    // TODO:
    DirectoryProperty getTestedWorkingDirectory();

    /**
     * @return Exclude patterns for files included in this sample
     */
    ListProperty<String> getExcludes();

    /**
     * @return Gets the validation report for this sample.
     */
    RegularFileProperty getValidationReport();

    /**
     * @return A zip containing this sample.  This is the primary thing produced by a sample for a given language.
     */
    RegularFileProperty getZipFile();

    /**
     * @return A installation directory containing this sample.  This can be used to get an installed version of the sample.
     */
    DirectoryProperty getInstallDirectory();

    /**
     * TODO:
     */
    DirectoryProperty getTestedInstallDirectory();
}
