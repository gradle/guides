package org.gradle.docs.internal.exemplar;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.workers.WorkParameters;

public interface AsciidoctorContentTestParameters extends WorkParameters {
    ConfigurableFileCollection getContentFiles();

    DirectoryProperty getWorkspaceDirectory();

    DirectoryProperty getGradleUserHomeDirectory();
}
