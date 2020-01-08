package org.gradle.docs.internal.exemplar;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.SafeMode;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecOperations;
import org.gradle.samples.loader.asciidoctor.AsciidoctorSamplesDiscovery;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;
import org.gradle.samples.test.normalizer.AsciidoctorAnnotationNormalizer;
import org.gradle.samples.test.normalizer.GradleOutputNormalizer;
import org.gradle.samples.test.normalizer.OutputNormalizer;
import org.gradle.samples.test.normalizer.StripTrailingOutputNormalizer;
import org.gradle.samples.test.normalizer.TrailingNewLineOutputNormalizer;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.workers.WorkAction;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.ComparisonFailure;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.exemplar.OutputNormalizers.composite;
import static org.gradle.docs.internal.exemplar.OutputNormalizers.toFunctional;
import static org.hamcrest.core.StringStartsWith.startsWith;

public abstract class AsciidoctorContentTestWorkerAction implements WorkAction<AsciidoctorContentTestParameters> {
    private static final Logger LOGGER = Logging.getLogger(AsciidoctorContentTestWorkerAction.class);

    @Override
    public void execute() {
        getParameters().getContentFiles().forEach(f -> {
            try {
                List<Sample> samples = AsciidoctorSamplesDiscovery.extractFromAsciidoctorFile(f, it -> {
                    it.safe(SafeMode.UNSAFE);
                    it.attributes(AttributesBuilder.attributes().attribute("verbose", 0));
                });
                List<Command> commands = samples.stream().map(Sample::getCommands).collect(ArrayList::new, List::addAll, List::addAll);
                LOGGER.info("Testing " + commands.size() + " commands on " + f.getAbsolutePath());
                run(commands);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (ComparisonFailure e) {
                throw new RuntimeException(e.getMessage() + "\nExpected:" + e.getExpected() + "\n\nActual: " + e.getActual());
            }
        });
    }

    @Inject
    protected abstract ExecOperations getExecOperations();

    // TODO: This code need to be consolidated with Exemplar. There is some overlap and duplication.
    private void run(List<Command> commands) throws IOException {
        File workspace = Files.createTempDirectory("exemplar").toFile();
        File baseWorkingDir = new File(workspace, "working");
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

            if (command.getExecutable().contains("gradle")) {
                disableWelcomeMessage(gradleUserHomeDir);
                if (command.getArgs().get(0).equals("init")) {
                    ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(workingDir).useGradleUserHomeDir(gradleUserHomeDir).connect();
                    CancellationTokenSource cancel = GradleConnector.newCancellationTokenSource();
                    OutputNormalizer normalizer = composite(new GradleOutputNormalizer(), new StripTrailingOutputNormalizer());
                    String expectedOutput = normalizer.normalize(command.getExpectedOutput(), null);

                    ByteArrayOutputStream fullOutputStream = new ByteArrayOutputStream();
                    PipedOutputStream inBackend = new PipedOutputStream();
                    PipedInputStream stdinInputToOutputStreamAdapter = new PipedInputStream(inBackend);
                    TeeInputStream stdinForToolingApi = new TeeInputStream(stdinInputToOutputStreamAdapter, fullOutputStream);

                    PipedInputStream outBackend = new PipedInputStream();
                    PipedOutputStream stdoutOutputToInputStreamAdapter = new PipedOutputStream(outBackend);
                    TeeOutputStream stdoutForToolingApi = new TeeOutputStream(stdoutOutputToInputStreamAdapter, fullOutputStream);

                    try {
                        AssertingResultHandler resultHandler = new AssertingResultHandler();
                        connection.newBuild().forTasks(command.getArgs().toArray(new String[0])).setStandardInput(stdinForToolingApi).setStandardOutput(stdoutForToolingApi).withCancellationToken(cancel.token()).run(resultHandler);

                        OutputConsumer c = new OutputConsumer(expectedOutput);
                        try {
                            Function<InputStream, Void> interactiveChain = debounceStdOut(Duration.ofMillis(1500)).andThen(toFunctional(normalizer)).andThen(userInputFromExpectedOutput(c)).andThen(writeToStdIn(inBackend));

                            while (c.hasMoreOutput()) {
                                interactiveChain.apply(outBackend);
                            }
                        } catch (Throwable e) {
                            cancel.cancel();
                            throw e;
                        }

                        try {
                            resultHandler.waitFor(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        resultHandler.assertCompleteSuccessfully();

                        String output = fullOutputStream.toString();
                        Assert.assertEquals("Output not equals", expectedOutput, normalizer.normalize(output, null));
                    } finally {
                        connection.close();
                    }
                } else {
                    final File workDir = workingDir;
                    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    getExecOperations().exec(spec -> {
                        spec.executable(command.getExecutable());
                        spec.args(command.getArgs());
                        spec.args("--gradle-user-home=" + gradleUserHomeDir.getAbsolutePath());
                        spec.setWorkingDir(workDir);
                        spec.setStandardOutput(outStream);
                    });
                    String expectedOutput = command.getExpectedOutput();
                    OutputNormalizer normalizer = new GradleOutputNormalizer();
                    expectedOutput = normalizer.normalize(expectedOutput, null);
                    String output = outStream.toString();
                    output = normalizer.normalize(output, null);
                    Assert.assertEquals("Output not equals", expectedOutput, output);
                }
            } else {
                final File workDir = workingDir;
                final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                getExecOperations().exec(spec -> {
                    spec.executable(command.getExecutable());
                    spec.args(command.getArgs());
                    spec.setWorkingDir(workDir);
                    spec.setStandardOutput(outStream);
                });

                if (!command.getExpectedOutput().isEmpty()) {
                    String expectedOutput = command.getExpectedOutput();
                    OutputNormalizer normalizer = composite(new AsciidoctorAnnotationNormalizer(), new TrailingNewLineOutputNormalizer());
                    expectedOutput = normalizer.normalize(expectedOutput, null);
                    String output = outStream.toString();
                    output = normalizer.normalize(output, null);
                    Assert.assertEquals("Output no equals", expectedOutput, output);
                }
            }
        }
    }

    private void disableWelcomeMessage(File gradleUserHomeDirectory) {
        File welcomeMessageRenderedFile = new File(gradleUserHomeDirectory, "notifications/6.0.1/release-features.rendered");
        welcomeMessageRenderedFile.getParentFile().mkdirs();
        try {
            welcomeMessageRenderedFile.createNewFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static UnaryOperator<String> userInputFromExpectedOutput(OutputConsumer output) {
        return incoming -> {
            output.consumeOutput(incoming);

            String input = output.consumeNextInput();
            return input;
        };
    }

    private static Function<String, Void> writeToStdIn(OutputStream stdin) {
        return incoming -> {
            try {
                stdin.write(incoming.getBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return null;
        };
    }

    private static Function<InputStream, String> debounceStdOut(Duration debounce) {
        return stdout -> {
            try {
                int lastAvailable = 0;
                long quietPeriod = Long.MAX_VALUE;
                for (; ; ) {
                    int a = stdout.available();
                    if (a > 0 && a > lastAvailable) {
                        lastAvailable = a;
                        quietPeriod = System.nanoTime() + debounce.toNanos();
                    }

                    long d = System.nanoTime() - quietPeriod;
                    if (d > 0 && lastAvailable > 0) {
                        byte[] incomingBytes = new byte[lastAvailable];
                        stdout.read(incomingBytes);
                        String incoming = new String(incomingBytes);
                        return incoming;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static class OutputConsumer {
        private String output;

        public OutputConsumer(String output) {
            this.output = output;
        }

        public void consumeOutput(String outputSnippet) {
            LOGGER.info("==== CONSUMING OUTPUT (" + outputSnippet.length() + " characters) ====\n" + outputSnippet + "\n====");

            Assert.assertThat("Consuming the received output from the expected output", output, startsWith(outputSnippet));

            output = output.substring(outputSnippet.length());

        }

        public String consumeNextInput() {
            int idx = output.indexOf('\n');
            String input = output.substring(0, idx + 1);

            // We strip leading whitespace as we are stripping the tailing whitespace from the received output
            input = stripLeading(input);
            LOGGER.info("---- USING INPUT ----\n" + input + "\n----");
            Assert.assertThat("Consumed user input is too large to make sense", input, isSensibleSize());

            output = output.substring(idx + 1);

            return input;
        }

        public boolean hasMoreOutput() {
            return !output.isEmpty();
        }

        private static Matcher<String> isSensibleSize() {
            return new BaseMatcher<String>() {
                @Override
                public void describeTo(Description description) {
                    description.appendText("String longer than 3 characters.");
                }

                @Override
                public boolean matches(Object item) {
                    return ((String)item).length() <= 3;
                }
            };
        }

        private static String stripLeading(String self) {
            int len = self.length();
            int st = 0;
            char[] val = self.toCharArray();    /* avoid getfield opcode */

            while ((st < len) && (Character.isSpaceChar(val[st]))) {
                st++;
            }
            return ((st > 0)) ? self.substring(st, len - st + 1) : self;
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
            } while (finished || waitTimeoutMs > 0);

            return finished;
        }

        public void assertCompleteSuccessfully() {
            Assert.assertTrue("Gradle execution hasn't completed yet.", finished);
            Assert.assertNull("Gradle completed with an exception.", exception);
        }
    }
}
