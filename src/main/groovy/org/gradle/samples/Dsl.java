package org.gradle.samples;

public enum Dsl {
    GROOVY("Groovy"), KOTLIN("Kotlin");

    private final String displayName;

    Dsl(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
