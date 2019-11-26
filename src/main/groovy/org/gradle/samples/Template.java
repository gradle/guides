package org.gradle.samples;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public interface Template extends Named {
    @Override
    String getName();

    /**
     *
     */
    DirectoryProperty getSourceDirectory();

    Property<String> getTarget();

    DirectoryProperty getTemplateDirectory();
}
