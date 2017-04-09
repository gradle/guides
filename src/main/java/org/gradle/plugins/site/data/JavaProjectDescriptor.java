package org.gradle.plugins.site.data;

import org.gradle.api.tasks.Input;

public class JavaProjectDescriptor {
    private final String sourceCompatibility;
    private final String targetCompatibility;

    public JavaProjectDescriptor(String sourceCompatibility, String targetCompatibility) {
        this.sourceCompatibility = sourceCompatibility;
        this.targetCompatibility = targetCompatibility;
    }

    @Input
    public String getSourceCompatibility() {
        return sourceCompatibility;
    }

    @Input
    public String getTargetCompatibility() {
        return targetCompatibility;
    }
}
