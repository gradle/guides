package org.gradle.docs.internal;

import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {
    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    public static String uncapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String toTitleCase(String s) {
        return Arrays.stream(GUtil.toWords(s).split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public static String toLowerCamelCase(String s) {
        return uncapitalize(toTitleCase(s));
    }
    public static String toSnakeCase(String s) {
        return s.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }
    public static String toKebabCase(String text) {
        Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "-"+m.group().toLowerCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
