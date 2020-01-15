package org.gradle.docs.samples.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.docs.internal.components.NamedDocumentationComponent;

import javax.inject.Inject;

public abstract class SampleExemplarBinary extends NamedDocumentationComponent {
    @Inject
    public SampleExemplarBinary(String name) {
        super(name);
    }

    public abstract DirectoryProperty getTestedInstallDirectory();

    public abstract DirectoryProperty getTestedWorkingDirectory();

    public abstract ConfigurableFileCollection getTestsContent();
}
