package org.gradle.docs.samples.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;

import javax.inject.Inject;

public abstract class SampleExemplarBinary extends SampleBinary {
    @Inject
    public SampleExemplarBinary(String name) {
        super(name);
    }

    public abstract DirectoryProperty getTestedInstallDirectory();

    public abstract DirectoryProperty getTestedWorkingDirectory();

    public abstract ConfigurableFileCollection getTestsContent();
}
