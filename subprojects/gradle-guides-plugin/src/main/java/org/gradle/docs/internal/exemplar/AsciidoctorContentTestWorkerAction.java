package org.gradle.docs.internal.exemplar;

import org.asciidoctor.SafeMode;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecOperations;
import org.gradle.samples.executor.ExecutionMetadata;
import org.gradle.samples.loader.asciidoctor.AsciidoctorCommandsDiscovery;
import org.gradle.samples.model.Command;
import org.gradle.samples.test.normalizer.AsciidoctorAnnotationOutputNormalizer;
import org.gradle.samples.test.normalizer.GradleOutputNormalizer;
import org.gradle.samples.test.normalizer.OutputNormalizer;
import org.gradle.samples.test.normalizer.StripTrailingOutputNormalizer;
import org.gradle.samples.test.normalizer.TrailingNewLineOutputNormalizer;
import org.gradle.samples.test.normalizer.WorkingDirectoryOutputNormalizer;
import org.gradle.samples.test.verifier.AnyOrderLineSegmentedOutputVerifier;
import org.gradle.samples.test.verifier.OutputVerifier;
import org.gradle.samples.test.verifier.StrictOrderLineSegmentedOutputVerifier;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.workers.WorkAction;
import org.junit.Assert;
import org.junit.ComparisonFailure;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.exemplar.OutputNormalizers.composite;
import static org.junit.Assert.assertTrue;

public abstract class AsciidoctorContentTestWorkerAction implements WorkAction<AsciidoctorContentTestParameters> {
    private static final Logger LOGGER = Logging.getLogger(AsciidoctorContentTestWorkerAction.class);

    @Override
    public void execute() {
        getParameters().getTestCases().get().forEach(testCase -> {
            try {
                File f = testCase.getContentFile().get().getAsFile();
                List<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(f, it -> {
                    it.safe(SafeMode.UNSAFE);
                });

                if (commands.isEmpty()) {
                    LOGGER.info("No commands to test on " + f.getAbsolutePath());
                } else {
                    if (testCase.getStartingSample().isPresent()) {
                        File sampleSeedDirectory = testCase.getStartingSample().get().getAsFile();
                        LOGGER.info("Testing " + commands.size() + " commands on " + f.getAbsolutePath() + " with sample from " + sampleSeedDirectory.getAbsolutePath());
                        run(commands, seedSample(sampleSeedDirectory));
                    } else {
                        LOGGER.info("Testing " + commands.size() + " commands on " + f.getAbsolutePath() + " without initial sample");
                        run(commands, seedEmptySample());
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (ComparisonFailure e) {
                throw new RuntimeException(e.getMessage() + "\nExpected:" + e.getExpected() + "\n\nActual: " + e.getActual());
            }
        });
    }

    private File seedSample(File source) throws IOException {
        File result = seedEmptySample();
        getFileOperations().copy(spec -> {
            spec.from(source);
            spec.into(result);
        });
        return result;
    }

    private File seedEmptySample() throws IOException {
        return Files.createTempDirectory("exemplar").toFile();
    }

    @Inject
    protected abstract FileSystemOperations getFileOperations();

    @Inject
    protected abstract ExecOperations getExecOperations();

    // TODO: This code need to be consolidated with Exemplar. There is some overlap and duplication.
    private void run(List<Command> commands, File baseWorkingDir) throws IOException {
        File homeDirectory = baseWorkingDir;
        File gradleUserHomeDir = getParameters().getGradleUserHomeDirectory().get().getAsFile();

        baseWorkingDir.mkdirs();
        gradleUserHomeDir.mkdirs();

        for (Command command : commands) {
            File workingDir = baseWorkingDir;

            if (command.getExecutionSubdirectory() != null) {
                workingDir = new File(workingDir, command.getExecutionSubdirectory());
            }

            LOGGER.info("Executing  command '" + command.getExecutable() + " " + command.getArgs().stream().collect(Collectors.joining(" ")) + "' inside '" + workingDir.getAbsolutePath() + "'");

            // This should be some kind of plugable executor rather than hard-coded here
            if (command.getExecutable().equals("cd")) {
                baseWorkingDir = new File(baseWorkingDir, command.getArgs().get(0)).getCanonicalFile();
                continue;
            }

            // TODO: For better rendering, we should allow code block to resolve attributes using `subs=attributes`. However, exemplar doesn't resolve the attributes according to this configuration. The value returned by `Command.#getExpectedOutput()` will contain the attribute syntax which will fail the output verification. For now, we will leave this as a manual steps that will need to be executed when we want to change this value. We should honor this configuration.
            if (command.getExecutable().contains("gradle")) {
                disableWelcomeMessage(gradleUserHomeDir);
                primeGradleUserHome();

                try (ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(workingDir).useGradleUserHomeDir(gradleUserHomeDir).useGradleVersion(getParameters().getGradleVersion().get()).connect()) {
                    if (command.getArgs().get(0).equals("init") || command.getArgs().contains("--scan")) {
                        CancellationTokenSource cancel = GradleConnector.newCancellationTokenSource();
                        OutputNormalizer normalizer = composite(new GradleOutputNormalizer(), new StripTrailingOutputNormalizer());
                        String expectedOutput = normalizer.normalize(command.getExpectedOutput(), null);
                        expectedOutput += "\n\n"; // Adding new lines because Asciidoctor strip trailing new lines (we can have more but not less for the interactive process to work)

                        ByteArrayOutputStream fullOutputStream = new ByteArrayOutputStream();

                        AssertingResultHandler resultHandler = new AssertingResultHandler();
                        // TODO: Configure environment variables
                        // TODO: The following won't work for flags with arguments
                        connection.newBuild()
                                .forTasks(command.getArgs().stream().filter(it -> !it.startsWith("--scan")).collect(Collectors.toList()).toArray(new String[0]))
                                .withArguments(command.getArgs().stream().filter(it -> it.startsWith("--scan")).collect(Collectors.toList()))
                                .setStandardInput(new ByteArrayInputStream((command.getUserInputs().stream().collect(Collectors.joining(System.getProperty("line.separator"))) + System.getProperty("line.separator")).getBytes()))
                                .setStandardOutput(fullOutputStream)
                                .setStandardError(fullOutputStream)
                                .withCancellationToken(cancel.token())
                                .run(resultHandler);

                        try {
                            assertTrue(resultHandler.waitFor(5, TimeUnit.SECONDS));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        resultHandler.assertCompleteSuccessfully();

                        normalizer = composite(normalizer, new TrailingNewLineOutputNormalizer());
                        String output = normalizer.normalize(fullOutputStream.toString(), null);
                        expectedOutput = normalizer.normalize(expectedOutput, null);

                        OutputVerifier verifier = new UserInputOutputVerifier(command.getUserInputs());
                        verifier.verify(expectedOutput, output, command.isAllowAdditionalOutput());
                    } else {
                        AsciidoctorContentTestConsoleType consoleType = consoleTypeOf(command);
                        try (OutputStream outStream = newOutputCapturingStream(consoleType)) {
                            boolean richOutput = false;
                            if (command.getArgs().stream().noneMatch(it -> it.startsWith("--console="))) {
                                if (consoleType == AsciidoctorContentTestConsoleType.VERBOSE) {
                                    throw new RuntimeException("--console=verbose is no supported");
                                } else if (consoleType == AsciidoctorContentTestConsoleType.RICH) {
                                    richOutput = true;
                                }
                            }

                            connection.newBuild()
                                    .forTasks(command.getArgs().toArray(new String[0]))
                                    .setColorOutput(richOutput)
                                    .setStandardOutput(outStream)
                                    .setStandardError(outStream)
                                    .run();

                            String expectedOutput = command.getExpectedOutput();
                            OutputNormalizer normalizer = composite(new GradleOutputNormalizer(), new WorkingDirectoryOutputNormalizer(), new GradleUserHomePathOutputNormalizer(gradleUserHomeDir), new TrailingNewLineOutputNormalizer());
                            ExecutionMetadata executionMetadata = new ExecutionMetadata(homeDirectory, Collections.emptyMap());
                            expectedOutput = normalizer.normalize(expectedOutput, executionMetadata);
                            String output = outStream.toString();
                            output = normalizer.normalize(output, executionMetadata);

                            OutputVerifier verifier = new AnyOrderLineSegmentedOutputVerifier();
                            verifier.verify(expectedOutput, output, command.isAllowAdditionalOutput());
                        }
                    }
                }
            } else {
                final File workDir = workingDir;
                final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                getExecOperations().exec(spec -> {
                    spec.executable(command.getExecutable());
                    spec.args(command.getArgs());
                    spec.setWorkingDir(workDir);
                    spec.environment("HOME", homeDirectory.getAbsolutePath());
                    spec.setStandardOutput(outStream);
                    spec.setErrorOutput(outStream);
                });

                if (!command.getExpectedOutput().isEmpty()) {
                    String expectedOutput = command.getExpectedOutput();
                    OutputNormalizer normalizer = composite(new AsciidoctorAnnotationOutputNormalizer(), new TrailingNewLineOutputNormalizer());
                    expectedOutput = normalizer.normalize(expectedOutput, null);
                    String output = outStream.toString();
                    output = normalizer.normalize(output, null);

                    OutputVerifier verifier = new StrictOrderLineSegmentedOutputVerifier();
                    verifier.verify(expectedOutput, output, command.isAllowAdditionalOutput());
                }
            }
        }
    }

    private AsciidoctorContentTestConsoleType consoleTypeOf(Command command) {
        AsciidoctorContentTestConsoleType consoleType = getParameters().getDefaultConsoleType().getOrElse(AsciidoctorContentTestConsoleType.RICH);
        if (command.getArgs().stream().anyMatch(it -> it.startsWith("--console=verbose"))) {
            consoleType = AsciidoctorContentTestConsoleType.VERBOSE;
        } else if (command.getArgs().stream().anyMatch(it -> it.startsWith("--console=plain"))) {
            consoleType = AsciidoctorContentTestConsoleType.PLAIN;
        }

        return consoleType;
    }

    public OutputStream newOutputCapturingStream(AsciidoctorContentTestConsoleType consoleType) {
        if (consoleType == AsciidoctorContentTestConsoleType.PLAIN) {
            return new ByteArrayOutputStream();
        }
        return new AnsiCharactersToPlainTextOutputStream();
    }

    private void disableWelcomeMessage(File gradleUserHomeDirectory) {
        File welcomeMessageRenderedFile = new File(gradleUserHomeDirectory, "notifications/" + getParameters().getGradleVersion().get() + "/release-features.rendered");
        welcomeMessageRenderedFile.getParentFile().mkdirs();
        try {
            welcomeMessageRenderedFile.createNewFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void primeGradleUserHome() throws IOException {
        File workingDir = Files.createTempDirectory("exemplar").toFile();
        try (ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(workingDir).useGradleUserHomeDir(getParameters().getGradleUserHomeDirectory().get().getAsFile()).useGradleVersion(getParameters().getGradleVersion().get()).connect()) {
            connection.newBuild().forTasks("help").run();
        }
    }

    private static class AssertingResultHandler implements ResultHandler<Void> {
        private boolean finished = false;
        private GradleConnectionException exception;

        @Override
        public void onComplete(Void result) {
            finished = true;
        }

        @Override
        public void onFailure(GradleConnectionException failure) {
            exception = failure;
            finished = true;
        }

        /**
         * @param timeout the maximum time to wait
         * @param unit the time unit of the {@code timeout} argument
         * @return {@code true} if the Gradle build has exited and {@code false} if the waiting time elapsed before the Gradle build has exited.
         * @throws InterruptedException if the current thread is interrupted while waiting.
         */
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            if (finished) {
                return true;
            }

            long waitTimeoutMs = unit.toMillis(timeout);
            do {
                Thread.sleep(1000);
                waitTimeoutMs -= 1000;
            } while (!finished || waitTimeoutMs > 0);

            return finished;
        }

        public void assertCompleteSuccessfully() {
            assertTrue("Gradle execution hasn't completed yet.", finished);
            Assert.assertNull("Gradle completed with an exception.", exception);
        }
    }
}
