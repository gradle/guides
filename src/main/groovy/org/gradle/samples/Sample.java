package org.gradle.samples;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

/**
 * Represent a sample to be documented. Each sample must contain at least a Groovy or Kotlin DSL sample.
 */
public interface Sample extends Named {
    @Internal
    @Override
    String getName();

    /**
     * Property for configuring the sample root directory.
     *
     * By convention, this is the sample name off the extension's sample root directory.
     */
    @Internal
    DirectoryProperty getSampleDirectory();

    /**
     * The README file for the sample.
     *
     * By convention, this is README.adoc in the sample directory.
     */
    @Internal
    RegularFileProperty getReadMeFile();

    /**
     * The LICENSE file for the sample.
     *
     * By convention, this is LICENSE in the sample directory.
     */
    @Internal
    RegularFileProperty getLicenseFile();

    /**
     * Property for configuring the sample description. The description is used within the sample index.
     */
    @Input
    Property<String> getDescription();

    /**
     * Property for configuring the sample display name. The display name is used within the sample index.
     */
    @Input
    Property<String> getDisplayName();

    /**
     * Sample content that is shared by all DSLs.
     *
     * By convention, this is the wrapper files, README and LICENSE.
     */
    @Internal
    ConfigurableFileCollection getCommonContent();

    /**
     * Configure common content.
     */
    void common(Action<? super ConfigurableFileCollection> action);

    /**
     * Sample content that is used for Groovy DSL.
     *
     * By convention, this is the "groovy" directory in the sample directory.
     */
    @Internal
    ConfigurableFileCollection getGroovyContent();
    /**
     * Configure Groovy content.
     */
    void groovy(Action<? super ConfigurableFileCollection> action);

    /**
     * Sample content that is used for Groovy DSL.
     */
    @Internal
    ConfigurableFileCollection getKotlinContent();
    /**
     * Configure Kotlin content.
     */
    void kotlin(Action<? super ConfigurableFileCollection> action);

    /**
     * Configure which DSLs should be expected for this sample.
     * By convention, this is both Groovy and Kotlin.
     * Every sample must have at least one DSL.
     */
    @Input
    ListProperty<Dsl> getDsls();

    /**
     * Root installation directory for each DSL.
     */
    @Internal
    DirectoryProperty getInstallDirectory();

    /**
     * The generated sample (asciidoc) page.
     *
     * This is an asciidoc file, not the generated HTML.
     */
    @Internal
    RegularFileProperty getSamplePageFile();
}
