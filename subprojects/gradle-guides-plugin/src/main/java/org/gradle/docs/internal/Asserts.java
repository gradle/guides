package org.gradle.docs.internal;

import org.gradle.api.Named;

public class Asserts {
    public static void assertNameDoesNotContainsDisallowedCharacters(Named element, String format, Object... args) {
        if (element.getName().contains("_") || element.getName().contains("-")) {
            throw new IllegalArgumentException(String.format(format, args));
        }
    }
}
