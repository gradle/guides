package org.gradle.docs.internal;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {
    private static final Pattern UPPER_LOWER = Pattern.compile("(?m)([A-Z]*)([a-z0-9]*)");
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
        return Arrays.stream(toWords(s).split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    /**
     * Converts an arbitrary string to space-separated words.
     * Eg, camelCase -&gt; camel case, with_underscores -&gt; with underscores
     */
    private static String toWords(CharSequence string) {
        if (string == null) {
            return null;
        }
        char separator = ' ';
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        Matcher matcher = UPPER_LOWER.matcher(string);
        while (pos < string.length()) {
            matcher.find(pos);
            if (matcher.end() == pos) {
                // Not looking at a match
                pos++;
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(separator);
            }
            String group1 = matcher.group(1).toLowerCase();
            String group2 = matcher.group(2);
            if (group2.isEmpty()) {
                builder.append(group1);
            } else {
                if (group1.length() > 1) {
                    builder.append(group1, 0, group1.length() - 1);
                    builder.append(separator);
                    builder.append(group1.substring(group1.length() - 1));
                } else {
                    builder.append(group1);
                }
                builder.append(group2);
            }
            pos = matcher.end();
        }

        return builder.toString();
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
        return uncapitalize(String.join("", toTitleCase(s).split(" ")));
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
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, "-"+m.group().toLowerCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
