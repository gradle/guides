package org.gradle.docs.internal;

import org.gradle.api.file.RegularFileProperty;

public interface ViewableContentBinary {
    String getViewTaskName();

    RegularFileProperty getViewablePageFile();
}
