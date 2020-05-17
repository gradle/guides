package org.gradle.docs.samples.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.docs.samples.Dsl;

import javax.inject.Inject;

/**
 * Represents a sample tailored for a particular DSL.
 */
public abstract class SampleArchiveBinary extends SampleInstallBinary {
    @Inject
    public SampleArchiveBinary(String name) {
        super(name);
    }

    /**
     * @return The language this sample is written for
     */
    public abstract Property<Dsl> getDsl();

    /**
     * @return Gets the validation report for this sample.
     */
    public abstract RegularFileProperty getValidationReport();

    /**
     * @return A zip containing this sample.  This is the primary thing produced by a sample for a given language.
     */
    public abstract RegularFileProperty getZipFile();
}
