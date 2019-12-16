package org.gradle.guides.test.fixtures.validation;

import java.util.Scanner;
import java.util.regex.Pattern;

public class DefaultOutputValidator implements OutputValidator {

    private static final Pattern DEPRECATION_REGEX_PATTERN = Pattern.compile(".*\\s+deprecated.*");
    private static final Pattern STACK_TRACE_REGEX_PATTERN = Pattern.compile("\\s+(at\\s+)?([\\w.$_]+/)?[\\w.$_]+\\.[\\w$_ =\\+\'-]+\\(.+?\\)");

    @Override
    public void validate(String output) {
        Scanner scanner = new Scanner(output);
        int i = 1;

        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (DEPRECATION_REGEX_PATTERN.matcher(line).matches()) {
                    throw new AssertionError(String.format("Line %d contains a deprecation warning: %s%n=====%n%s%n=====%n", i, line, output));
                } else if (STACK_TRACE_REGEX_PATTERN.matcher(line).matches()) {
                    throw new AssertionError(String.format("Line %d contains an unexpected stack trace: %s%n=====%n%s%n=====%n", i, line, output));
                }

                i++;
            }
        } finally {
            scanner.close();
        }
    }
}
