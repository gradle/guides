package org.gradle.samples;

public enum Dsl {
    GROOVY("Groovy", "groovy"), KOTLIN("Kotlin", "kotlin");

    private final String displayName;
    private final String conventionalDirectory;

    Dsl(String displayName, String conventionalDirectory) {
        this.displayName = displayName;
        this.conventionalDirectory = conventionalDirectory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConventionalDirectory() {
        return conventionalDirectory;
    }
}
