package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;

public interface Sample extends Named {
    DirectoryProperty getSampleDir();

    Provider<String> getGradleVersion();
}
