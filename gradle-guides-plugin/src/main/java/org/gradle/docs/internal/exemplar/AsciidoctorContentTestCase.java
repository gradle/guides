package org.gradle.docs.internal.exemplar;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

public interface AsciidoctorContentTestCase {
    @InputDirectory @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    DirectoryProperty getStartingSample();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    RegularFileProperty getContentFile();
}
