package org.gradle.docs.internal.exemplar;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

public interface AsciidoctorContentTestCase {
    @InputDirectory @Optional
    DirectoryProperty getStartingSample();

    @InputFile
    RegularFileProperty getContentFile();
}
