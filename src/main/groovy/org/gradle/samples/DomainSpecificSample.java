package org.gradle.samples;

import org.gradle.api.file.ConfigurableFileCollection;

public interface DomainSpecificSample {
    ConfigurableFileCollection getArchiveContent();
}
