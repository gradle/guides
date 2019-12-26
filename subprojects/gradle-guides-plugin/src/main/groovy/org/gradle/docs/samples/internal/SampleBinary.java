package org.gradle.docs.samples.internal;

import org.gradle.api.Named;
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
public abstract class SampleBinary implements Named {
    private final String name;

    @Inject
    public SampleBinary(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The language this sample is written for
     */
    public abstract Property<Dsl> getDsl();

    /**
     * @return All content to include in the sample.
     */
    public abstract ConfigurableFileCollection getContent();

    /**
     * @return Content that is specific to the DSL language.
     */
    public abstract ConfigurableFileCollection getDslSpecificContent();

    /**
     * @return Content that is needed to test the sample.
     */
    public abstract ConfigurableFileCollection getTestsContent();

    /**
     * @return The documentation page for this sample
     */
    public abstract Property<String> getSampleLinkName();

    /**
     * @return Working directory used by the plugin to expose an assembled sample to consumers.
     */
    public abstract DirectoryProperty getWorkingDirectory();

    /**
     * @return Working directory used to expose an assembled sample to tests.
     */
    public abstract DirectoryProperty getTestedWorkingDirectory();

    /**
     * @return Exclude patterns for files included in this sample
     */
    public abstract ListProperty<String> getExcludes();

    /**
     * @return Gets the validation report for this sample.
     */
    public abstract RegularFileProperty getValidationReport();

    /**
     * @return A zip containing this sample.  This is the primary thing produced by a sample for a given language.
     */
    public abstract RegularFileProperty getZipFile();

    /**
     * @return A installation directory containing this sample.  This can be used to get an installed version of the sample.
     */
    public abstract DirectoryProperty getInstallDirectory();

    /**
     * @return A installation directory containing this sample.  This can be used to get an installed version of the sample.
     */
    public abstract DirectoryProperty getTestedInstallDirectory();
}
