package org.gradle.samples;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

/**
 * Represent a sample to be documented. Each sample must contains a Groovy or Kotlin DSL sample.
 */
public interface Sample extends Named, ExtensionAware {
    /**
     * Property for configuring the sample root directory.
     *
     * @return a property for the sample root directory
     */
    DirectoryProperty getSampleDirectory();

    /**
     * Provider for locating the README file for the sample.
     *
     * @return a provider for the README file location
     */
    Provider<RegularFile> getReadMeFile();

    /**
     * Property for configuring the sample description. The description is used within the sample index.
     *
     * @return a property for the sample description
     */
    Property<String> getDescription();

    /**
     * Property for configuring the sample display name. The display name is used within the sample index.
     *
     * @return a property for the sample display name
     */
    Property<String> getDisplayName();

    /**
     * Property for configuring the Gradle version wrapper to generate for this sample.
     *
     * @return a property for the Gradle version wrapper to generate
     */
    Property<String> getGradleVersion();

    /**
     * Property for configuring the common content to include in both domain specific sample archives.
     *
     * @return a file collection to configure the files to include in the domain specific archives
     */
    ConfigurableFileCollection getArchiveContent();

    /**
     * Property for configuring the sample permalink.
     *
     * @return a property for the sample permalink
     */
    Property<String> getPermalink();

    /**
     * Explicitly declare a Groovy DSL sample at the conventional location (e.g. {@code src/samples/<name>/groovy}).
     *
     * NOTE: Explicitly declaring a domain specific sample exclude any implicit conventional declaration.
     */
    void withGroovyDsl();

    /**
     * Explicitly declare a Groovy DSL sample at a custom location.
     *
     * NOTE: Explicitly declaring a domain specific sample exclude any implicit conventional declaration.
     *
     * @param action the configuration action for the Groovy DSL
     */
    void withGroovyDsl(Action<? super DomainSpecificSample> action);

    /**
     * Explicitly declare a Kotlin DSL sample at the conventional location (e.g. {@code src/samples/<name>/kotlin}).
     *
     * NOTE: Explicitly declaring a domain specific sample exclude any implicit conventional declaration.
     */
    void withKotlinDsl();

    /**
     * Explicitly declare a Kotlin DSL sample at a custom location.
     *
     * NOTE: Explicitly declaring a domain specific sample exclude any implicit conventional declaration.
     *
     * @param action the configuration action for the Kotlin DSL
     */
    void withKotlinDsl(Action<? super DomainSpecificSample> action);

    /**
     * Returns the Asciidoctor task that process the README.adoc file.
     *
     * @return a provider for the Asciidoctor task of the sample README.adoc
     */
    TaskProvider<AsciidoctorTask> getAsciidoctorTask();
}
