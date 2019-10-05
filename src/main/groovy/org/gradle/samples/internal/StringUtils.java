package org.gradle.samples.internal;

public class StringUtils {
    public static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }
}
