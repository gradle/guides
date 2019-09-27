package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;

public interface Sample extends Named {
    DirectoryProperty getSampleDir();
}
