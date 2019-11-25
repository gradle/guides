package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;

public interface SampleBinary extends Named {
    @Input
    Property<Dsl> getDsl();

    @Internal
    ConfigurableFileCollection getContent();
    @Internal
    RegularFileProperty getSamplePageFile();
    @Internal
    DirectoryProperty getStagingDirectory();

    @InputFile
    RegularFileProperty getZipFile();
    @InputFiles
    DirectoryProperty getInstallDirectory();
}
