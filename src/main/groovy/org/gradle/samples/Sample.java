package org.gradle.samples;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskProvider;

/**
 * Represent a sample to be documented. Each sample must contains a Groovy or Kotlin DSL sample.
 */
public interface Sample extends Named {
    @Internal
    @Override
    String getName();

    /**
     * Property for configuring the sample root directory.
     *
     * @return a property for the sample root directory
     */
    @Internal
    DirectoryProperty getSampleDirectory();

    /**
     * Provider for locating the README file for the sample.
     *
     * @return a provider for the README file location
     */
    @Internal
    RegularFileProperty getReadMeFile();

    @Internal
    RegularFileProperty getLicenseFile();

    /**
     * Property for configuring the sample description. The description is used within the sample index.
     *
     * @return a property for the sample description
     */
    @Input
    Property<String> getDescription();

    /**
     * Property for configuring the sample display name. The display name is used within the sample index.
     *
     * @return a property for the sample display name
     */
    @Input
    Property<String> getDisplayName();

    @Internal
    ConfigurableFileCollection getCommonContent();
    void common(Action<? super ConfigurableFileCollection> action);

    @Internal
    ConfigurableFileCollection getGroovyContent();
    void groovy(Action<? super ConfigurableFileCollection> action);

    @Internal
    ConfigurableFileCollection getKotlinContent();
    void kotlin(Action<? super ConfigurableFileCollection> action);

    @Input
    ListProperty<Dsl> getDsls();

    @Internal
    DirectoryProperty getInstallDirectory();

    @Internal
    RegularFileProperty getSamplePageFile();
}
