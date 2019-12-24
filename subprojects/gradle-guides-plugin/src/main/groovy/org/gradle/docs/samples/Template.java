package org.gradle.docs.samples;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public interface Template extends Named {
    /**
     * @return Source directory of the template
     */
    DirectoryProperty getSourceDirectory();

    /**
     * @return subdirectory to place the template in
     */
    Property<String> getTarget();

    /**
     * @return destination directory for the template
     */
    DirectoryProperty getTemplateDirectory();
}
