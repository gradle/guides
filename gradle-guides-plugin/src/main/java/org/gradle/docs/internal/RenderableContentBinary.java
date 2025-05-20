package org.gradle.docs.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.provider.Property;

public interface RenderableContentBinary {
    Property<CopySpec> getResourceSpec();

    ConfigurableFileCollection getResourceFiles();

    Property<String> getSourcePattern();
}
