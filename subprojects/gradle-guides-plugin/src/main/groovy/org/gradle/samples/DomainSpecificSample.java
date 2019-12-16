package org.gradle.samples;

import org.gradle.api.file.ConfigurableFileCollection;

public interface DomainSpecificSample {
    /**
     * Property for configuring the content of single domain specific archive.
     *
     * @return  a file collection to configure the files to include in the domain specific archive
     */
    ConfigurableFileCollection getArchiveContent();
}
