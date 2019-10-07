package org.gradle.samples;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public interface Sample extends Named {
    DirectoryProperty getSampleDir();

    Property<String> getGradleVersion();

    ConfigurableFileCollection getArchiveContent();

    void withGroovyDsl();

    void withGroovyDsl(Action<? super DomainSpecificSample> action);

    void withKotlinDsl();

    void withKotlinDsl(Action<? super DomainSpecificSample> action);
}
