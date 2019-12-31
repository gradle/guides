package org.gradle.docs.internal.exemplar;

import org.gradle.samples.test.normalizer.OutputNormalizer;

import java.util.function.UnaryOperator;

public class OutputNormalizers {
    @SafeVarargs
    public static OutputNormalizer composite(OutputNormalizer... normalizers) {
        return (commandOutput, executionMetadata) -> {
            for (OutputNormalizer normalizer : normalizers) {
                commandOutput = normalizer.normalize(commandOutput, executionMetadata);
            }
            return commandOutput;
        };
    }

    public static UnaryOperator<String> toFunctional(OutputNormalizer normalizer) {
        return s -> normalizer.normalize(s, null);
    }
}
