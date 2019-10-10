package org.gradle.samples;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

public interface Sample extends Named, ExtensionAware {
    DirectoryProperty getSampleDirectory();

    Property<String> getDescription();

    Property<String> getGradleVersion();

    ConfigurableFileCollection getArchiveContent();

    void withGroovyDsl();

    void withGroovyDsl(Action<? super DomainSpecificSample> action);

    void withKotlinDsl();

    void withKotlinDsl(Action<? super DomainSpecificSample> action);

    TaskProvider<AsciidoctorTask> getAsciidoctorTask();
}
