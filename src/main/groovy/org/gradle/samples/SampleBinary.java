package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;

/**
 * Represents a sample tailored for a particular DSL.
 */
public interface SampleBinary extends Named {
    @Override
    String getName();

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

    DirectoryProperty getStagingDirectory();

    /**
     * Exclude patterns for files included in this sample
     */
    ListProperty<String> getExcludes();

    /**
     * A zip containing this sample
     */
    RegularFileProperty getZipFile();

    /**
     * A installation directory containing this sample
     */
    DirectoryProperty getInstallDirectory();
}
