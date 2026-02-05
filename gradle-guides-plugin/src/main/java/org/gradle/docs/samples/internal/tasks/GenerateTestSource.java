package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;

public abstract class GenerateTestSource extends DefaultTask {
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        String content = """
            //CHECKSTYLE:OFF
            package org.gradle.exemplar;

            import org.gradle.exemplar.executor.ExecutionMetadata;
            import org.gradle.exemplar.test.normalizer.FileSeparatorOutputNormalizer;
            import org.gradle.exemplar.test.normalizer.JavaObjectSerializationOutputNormalizer;
            import org.gradle.exemplar.test.normalizer.GradleOutputNormalizer;
            import org.gradle.exemplar.test.normalizer.OutputNormalizer;
            import org.gradle.exemplar.test.runner.GradleSamplesRunner;
            import org.gradle.exemplar.test.runner.SamplesOutputNormalizers;
            import org.junit.runner.RunWith;

            @RunWith(GradleSamplesRunner.class)
            @SamplesOutputNormalizers({
                JavaObjectSerializationOutputNormalizer.class,
                FileSeparatorOutputNormalizer.class,
                GradleOutputNormalizer.class,
                ExemplarExternalSamplesFunctionalTest.FunctionalTestOutputNormalizer.class
            })
            public class ExemplarExternalSamplesFunctionalTest {
                public static class FunctionalTestOutputNormalizer implements OutputNormalizer {
                    @Override
                    public String normalize(final String commandOutput, final ExecutionMetadata executionMetadata) {
                        if (commandOutput == null || commandOutput.isEmpty()) {
                            return commandOutput;
                        }
                        // Remove non-deterministic advice printed by some Gradle versions/configurations.
                        // Example: "Consider enabling configuration cache to speed up this build"
                        return commandOutput.replaceAll("(?m)^[\\t ]*Consider enabling configuration cache.*(?:\\\\R|$)", "");
                    }
                }
            }
            """;
        try {
            Directory sourceDirectory = getOutputDirectory().dir("org/gradle/docs/samples/").get();
            sourceDirectory.getAsFile().mkdirs();
            Files.write(sourceDirectory.file("ExemplarExternalSamplesFunctionalTest.java").getAsFile().toPath(), content.getBytes());
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
