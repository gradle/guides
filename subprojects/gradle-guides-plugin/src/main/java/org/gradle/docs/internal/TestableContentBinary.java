package org.gradle.docs.internal;

import org.gradle.api.file.RegularFileProperty;

public interface TestableContentBinary {
    String getCheckLinksTaskName();

    RegularFileProperty getRenderedPageFile();
}
