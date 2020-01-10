package org.gradle.docs.internal.exemplar;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.workers.WorkParameters;

public interface AsciidoctorContentTestParameters extends WorkParameters {
    ListProperty<AsciidoctorContentTestCase> getTestCases();

    DirectoryProperty getWorkspaceDirectory();

    DirectoryProperty getGradleUserHomeDirectory();
}
