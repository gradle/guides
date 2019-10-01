package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public interface Sample extends Named {
    DirectoryProperty getSampleDir();

    Property<String> getGradleVersion();
}
