package org.gradle.docs.guides;

import org.gradle.api.file.ConfigurableFileCollection;

public interface GuidesDistribution {
    /**
     * This is HTML and resources.
     *
     * @return collection of rendered documentation
     */
    ConfigurableFileCollection getRenderedDocumentation();
}
