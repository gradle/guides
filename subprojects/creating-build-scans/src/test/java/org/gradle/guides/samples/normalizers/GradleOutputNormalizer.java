/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.guides.samples.normalizers;

import org.gradle.samples.test.normalizer.OutputNormalizer;
import org.gradle.samples.executor.ExecutionMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleOutputNormalizer implements OutputNormalizer {
    private static final Pattern STACK_TRACE_ELEMENT = Pattern.compile("\\s+(at\\s+)?([\\w.$_]+/)?[\\w.$_]+\\.[\\w$_ =+\'-<>]+\\(.+?\\)(\\x1B\\[0K)?");
    private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in( \\d+[smh]+)+");
    private static final Pattern BUILD_SCAN_URL_PATTERN = Pattern.compile("https://gradle.com/s/\\w+");

    public static final String DOWNLOAD_MESSAGE_PREFIX = "Download ";
    public static final String GENERATING_JAR_PREFIX = "Generating JAR file 'gradle-api-";
    public static final String BUILD_SCAN_URL_CONSTANT = "https://gradle.com/s/uniqueid";

    // Duplicating here to avoid use of Gradle's internal API
    public static final String STARTING_A_GRADLE_DAEMON_MESSAGE = "Starting a Gradle Daemon";
    public static final String DAEMON_WILL_BE_STOPPED_MESSAGE = "Daemon will be stopped at the end of the build";
    public static final String EXPIRING_DAEMON_MESSAGE = "Expiring Daemon because JVM Tenured space is exhausted";
    public static final String DEPRECATED_GRADLE_FEATURES_MESSAGE = "Deprecated Gradle features were used in this build, making it incompatible with Gradle";

    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        //commandOutput = commandOutput.replaceAll(executionMetadata.getTempSampleProjectDir().getAbsolutePath(), NORMALIZED_SAMPLES_PATH);
        List<String> result = new ArrayList<>();
        final List<String> lines = Arrays.asList(commandOutput.split("\\r?\\n"));
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            if (line.startsWith(GENERATING_JAR_PREFIX)) {
                i++;
            } else if (line.startsWith(DOWNLOAD_MESSAGE_PREFIX)) {
                i++;
            } else if (line.contains(STARTING_A_GRADLE_DAEMON_MESSAGE)) {
                // Remove the "daemon starting" message
                i++;
            } else if (line.contains(DAEMON_WILL_BE_STOPPED_MESSAGE)) {
                // Remove the "Daemon will be shut down" message
                i++;
            } else if (line.contains(EXPIRING_DAEMON_MESSAGE)) {
                // Remove the "Expiring Daemon" message
                i++;
            } else if (line.contains(DEPRECATED_GRADLE_FEATURES_MESSAGE)) {
                // Remove the "Deprecated Gradle features..." message and "See https://docs.gradle.org..."
                i+=2;
            } else if (BUILD_RESULT_PATTERN.matcher(line).matches()) {
                result.add(BUILD_RESULT_PATTERN.matcher(line).replaceFirst("BUILD $1 in 0s"));
                i++;
            } else if (BUILD_SCAN_URL_PATTERN.matcher(line).matches()) {
                result.add(BUILD_SCAN_URL_PATTERN.matcher(line).replaceFirst(BUILD_SCAN_URL_CONSTANT));
                i++;
            } else {
                result.add(line);
                i++;
            }
        }

        return result.stream().collect(Collectors.joining(System.lineSeparator()));
    }
}
