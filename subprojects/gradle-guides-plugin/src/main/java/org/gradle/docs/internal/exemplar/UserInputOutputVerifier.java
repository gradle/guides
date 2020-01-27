package org.gradle.docs.internal.exemplar;

import org.gradle.samples.test.verifier.OutputVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserInputOutputVerifier implements OutputVerifier {
    private final List<String> actualUserInputs;

    public UserInputOutputVerifier(List<String> actualUserInputs) {
        this.actualUserInputs = actualUserInputs;
    }

    public void verify(String expected, String actual, boolean allowAdditionalOutput) {
        List<String> expectedLines = Arrays.asList(expected.split("\\r?\\n"));
        List<String> actualLines = Arrays.asList(actual.split("\\r?\\n"));
        int expectedIndex = 0;
        int actualIndex = 0;
        int userInputIndex = 0;
        if (allowAdditionalOutput) {
            actualIndex = this.findFirstMatchingLine(actualLines, expectedLines.get(expectedIndex));
        }

        while(actualIndex < actualLines.size() && expectedIndex < expectedLines.size()) {
            String expectedLine = expectedLines.get(expectedIndex);
            String actualLine = actualLines.get(actualIndex);
            if (isAskingQuestionToUser(expectedLine, actualLine)) {
                String expectedUserInput = expectedLine.substring(actualLine.length()).trim();

                if (expectedUserInput.equals(actualUserInputs.get(userInputIndex))) {
                    userInputIndex++;

                    // Ensure the new line is empty demonstrating an user input
                    assertTrue(expectedLines.get(++expectedIndex).isEmpty());
                } else {
                    fail(String.format("Unexpected value at line %d.%nExpected: %s%nActual: %s%nActual output:%n%s%n", actualIndex + 1, expectedLine, actualLine, actual));
                }
            } else if (!expectedLine.equals(actualLine)) {
                fail(String.format("Unexpected value at line %d.%nExpected: %s%nActual: %s%nActual output:%n%s%n", actualIndex + 1, expectedLine, actualLine, actual));
            }

            ++actualIndex;
            ++expectedIndex;
        }

        if (actualIndex == actualLines.size() && expectedIndex < expectedLines.size()) {
            fail(String.format("Lines missing from actual result, starting at expected line %d.%nExpected: %s%nActual output:%n%s%n", expectedIndex, expectedLines.get(expectedIndex), actual));
        }

        if (!allowAdditionalOutput && actualIndex < actualLines.size() && expectedIndex == expectedLines.size()) {
            fail(String.format("Extra lines in actual result, starting at line %d.%nActual: %s%nActual output:%n%s%n", actualIndex + 1, actualLines.get(actualIndex), actual));
        }

    }

    private boolean isAskingQuestionToUser(String expectedLine, String actualLine) {
        // NOTE: This is very opinionated to build init user interaction.
        if ((actualLine.startsWith("Enter selection") || actualLine.startsWith("Project name") || actualLine.startsWith("Source package")) && expectedLine.startsWith(actualLine)) {
            return true;

        // NOTE: This will detect user input where expected lines contains an explicit user input. User input that use the default suggestion will not be detected and requires the previous, opinionated, check to be detected.
        } else if (!expectedLine.equals(actualLine) && expectedLine.startsWith(actualLine)) {
            return true;
        }

        // No user input detected
        return false;
    }

    private int findFirstMatchingLine(List<String> actualLines, String expected) {
        for(int index = 0; index < actualLines.size(); ++index) {
            if (actualLines.get(index).equals(expected)) {
                return index;
            }
        }

        return actualLines.size();
    }
}
