package org.gradle.docs.internal;

import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {
    /**
     * Capitalizes the first letter of the string.
     *
     * example to Example
     *
     * @param s the string
     * @return transformed string
     */
    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Makes the first letter of the string lowercase.
     *
     * Example to example
     *
     * @param s the string
     * @return transformed string
     */
    public static String uncapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Capitalizes every word in a string.
     *
     * this is an example to This Is An Example
     *
     * @param s the string
     * @return transformed string
     */
    public static String toTitleCase(String s) {
        return Arrays.stream(GUtil.toWords(s).split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    /**
     * Capitalizes every word in a string, removes all spaces and joins them together as one identifier.
     *
     * This is like a conventional Java identifier.
     *
     * this is an example to thisIsAnExample
     *
     * @param s the string
     * @return transformed string
     */
    public static String toLowerCamelCase(String s) {
        return uncapitalize(Arrays.stream(toTitleCase(s).split(" ")).collect(Collectors.joining()));
    }

    /**
     * Splits a string based on uppercase letters and rejoins them with underscores and makes the identifier lowercase.
     *
     * ThisIsAnExample to this_is_an_example
     *
     * @param s the string
     * @return transformed string
     */
    public static String toSnakeCase(String s) {
        return s.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    /**
     * Like {@link #toSnakeCase(String)}, except separated by hyphens.
     *
     * ThisIsAnExample to This-is-an-example
     *
     * @param s the string
     * @return transformed string
     */
    public static String toKebabCase(String s) {
        Matcher m = Pattern.compile("(?<=[a-z0-9])[A-Z]").matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "-"+m.group().toLowerCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
