package org.gradle.samples;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;

public interface ExemplarExtension {
    ConfigurableFileCollection getSource();

    void source(Action<? super CopySpec> action);
}
