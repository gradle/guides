package org.gradle.samples.internal;

import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

class StringUtils {
    static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }

    static String toTitleCase(String v) {
        return Arrays.stream(GUtil.toWords(v).split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }
}
