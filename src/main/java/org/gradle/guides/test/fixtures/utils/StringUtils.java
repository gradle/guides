package org.gradle.guides.test.fixtures.utils;

public final class StringUtils {

    private StringUtils() {}

    public static String join(String[] array, String separator) {
        StringBuilder joinedString = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                joinedString.append(separator);
            }

            joinedString.append(array[i]);
        }

        return joinedString.toString();
    }
}
