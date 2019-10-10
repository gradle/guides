package org.gradle.samples.internal;

class StringUtils {
    static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }
}
