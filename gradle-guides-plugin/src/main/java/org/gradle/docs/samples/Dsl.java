package org.gradle.docs.samples;

import java.util.Locale;

public enum Dsl {
    GROOVY("Groovy"), KOTLIN("Kotlin");

    private final String displayName;

    Dsl(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConventionalDirectory() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public String getDslLabel() {
        return getConventionalDirectory() + "-dsl";
    }
}
