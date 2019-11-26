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
    @Internal
    @Override
    String getName();

    /**
     * The language this sample is written for
     */
    @Input
    Property<Dsl> getDsl();

    /**
     * All content to include in the sample.
     */
    @Internal
    ConfigurableFileCollection getContent();

    /**
     * Content that is specific to the DSL language.
     */
    @Internal
    ConfigurableFileCollection getDslSpecificContent();

    /**
     * The documentation page for this sample
     */
    @Internal
    RegularFileProperty getSamplePageFile();

    @Internal
    DirectoryProperty getStagingDirectory();

    /**
     * Exclude patterns for files included in this sample
     */
    @Internal
    ListProperty<String> getExcludes();

    /**
     * A zip containing this sample
     */
    @InputFile
    RegularFileProperty getZipFile();

    /**
     * A installation directory containing this sample
     */
    @InputFiles
    DirectoryProperty getInstallDirectory();
}
