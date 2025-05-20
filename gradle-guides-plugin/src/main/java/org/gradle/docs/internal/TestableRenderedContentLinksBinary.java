package org.gradle.docs.internal;

import org.gradle.api.file.RegularFileProperty;

public interface TestableRenderedContentLinksBinary {
    String getCheckLinksTaskName();

    RegularFileProperty getRenderedPageFile();
}
