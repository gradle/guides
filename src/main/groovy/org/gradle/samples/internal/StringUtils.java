package org.gradle.samples.internal;

import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class StringUtils {
    static String capitalize(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }

    static String toTitleCase(String v) {
        return Arrays.stream(GUtil.toWords(v).split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    static String toKebabCase(String text) {
        Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "-"+m.group().toLowerCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
