package org.gradle.docs.samples;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

/**
 * Represent a sample to be documented. Each sample must contain at least a Groovy or Kotlin DSL sample.
 */
public interface Sample extends Named, SampleSummary {
    /**
     * By convention, this is the sample name off the extension's sample root directory.
     *
     * @return Property for configuring the sample root directory.
     */
    DirectoryProperty getSampleDirectory();

    /**
     * By Convention, this is README.adoc within the sample directory.
     *
     * @return Property for configuring the readme file for the sample in Asciidoctor format.
     */
    RegularFileProperty getReadmeFile();

    /**
     * @return Sample content that is shared by all DSLs.
     *
     * By convention, this is the wrapper files and LICENSE.
     */
    ConfigurableFileCollection getCommonContent();

    /**
     * Configure common content.
     *
     * @param action configuration action
     */
    void common(Action<? super ConfigurableFileCollection> action);

    /**
     * By convention, this is the "groovy" directory in the sample directory.
     *
     * @return Sample content that is used for Groovy DSL.
     */
    ConfigurableFileCollection getGroovyContent();

    /**
     * Configure Groovy content.
     *
     * @param action configuration action
     */
    void groovy(Action<? super ConfigurableFileCollection> action);

    /**
     * By convention, this is the "kotlin" directory in the sample directory.
     *
     * @return Sample content that is used for Kotlin DSL.
     */
    ConfigurableFileCollection getKotlinContent();

    /**
     * Configure Kotlin content.
     *
     * @param action configuration action
     */
    void kotlin(Action<? super ConfigurableFileCollection> action);

    /**
     * @return Sample content that is used for Exemplar testing
     */
    ConfigurableFileCollection getTestsContent();

    /**
     * Configure testing content.
     *
     * @param action configuration action
     */
    void tests(Action<? super ConfigurableFileCollection> action);
}
