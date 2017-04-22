package org.gradle.plugins.site.data;

import org.gradle.api.tasks.Input;

/**
 * The data descriptor for capturing environment information.
 */
public class EnvironmentDescriptor {
    private final String gradleVersion;

    public EnvironmentDescriptor(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    @Input
    public String getGradleVersion() {
        return gradleVersion;
    }
}
