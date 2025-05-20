package org.gradle.docs.samples.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class SampleInstallBinary extends SampleBinary {
    @Inject
    public SampleInstallBinary(String name) {
        super(name);
    }

    /**
     * @return Working directory used by the plugin to expose an assembled sample to consumers.
     */
    public abstract DirectoryProperty getWorkingDirectory();

    /**
     * @return The documentation page for this sample
     */
    public abstract Property<String> getSampleLinkName();

    /**
     * @return All content to include in the sample.
     */
    public abstract ConfigurableFileCollection getContent();

    /**
     * @return Content that is specific to the DSL language.
     */
    public abstract ConfigurableFileCollection getDslSpecificContent();

    /**
     * @return Exclude patterns for files included in this sample
     */
    public abstract ListProperty<String> getExcludes();

    /**
     * @return A installation directory containing this sample.  This can be used to get an installed version of the sample.
     */
    public abstract DirectoryProperty getInstallDirectory();
}
