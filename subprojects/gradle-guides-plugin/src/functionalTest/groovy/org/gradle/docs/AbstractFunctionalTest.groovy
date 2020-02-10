/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.gradle.docs

import org.gradle.docs.TestFile
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.*

abstract class AbstractFunctionalTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    TestFile projectDir
    TestFile buildFile
    TestFile settingsFile
    BuildResult result
    private String gradleVersion

    def setup() {
        projectDir = new TestFile(temporaryFolder.root)
        buildFile = new TestFile(temporaryFolder.newFile('build.gradle'))
        settingsFile = new TestFile(temporaryFolder.newFile('settings.gradle'))
        file("gradle.properties").text = "org.gradle.jvmargs=-XX:MaxMetaspaceSize=500m -Xmx500m"
    }

    protected ExecutionResult build(String... arguments) {
        result = createAndConfigureGradleRunner(arguments).build()
        return new ExecutionResult(result)
    }

    protected ExecutionResult buildAndFail(String... arguments) {
        result = createAndConfigureGradleRunner(arguments).buildAndFail()
        return new ExecutionResult(result)
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        def allArgs = (arguments as List) + ["-S"]
        def runner = GradleRunner.create().withProjectDir(projectDir).withArguments(allArgs).withPluginClasspath().forwardOutput().withDebug(true)
        if (gradleVersion != null) {
            runner.withGradleVersion(gradleVersion)
            gradleVersion = null
        }
        return runner
    }

    String getGradleVersion() {
        return gradleVersion ?: System.getProperty("gradle.version")
    }

    void usingGradleVersion(String gradleVersion) {
        this.gradleVersion = gradleVersion
    }

    static File createDir(File dir, String subDirName) {
        File newDir = new File(dir, subDirName)

        if (!newDir.mkdirs()) {
            throw new IOException("Unable to create directory " + subDirName)
        }

        newDir
    }

    TestFile file(Object... paths) {
        return projectDir.file(paths)
    }

    static class ExecutionResult implements BuildResult {
        private static final SKIPPED_TASK_OUTCOMES = [FROM_CACHE, UP_TO_DATE, SKIPPED, NO_SOURCE]

        @Delegate
        private final BuildResult delegate

        ExecutionResult(BuildResult delegate) {
            this.delegate = delegate
        }

        ExecutionResult assertTasksExecutedAndNotSkipped(Object... taskPaths) {
            assertTasksExecuted(taskPaths)
            assertTasksNotSkipped(taskPaths)
            return this
        }

        ExecutionResult assertTasksExecuted(Object... taskPaths) {
            def expectedTasks = taskPaths.flatten() as Set
            def actualTasks = tasks.collect { it.path } as Set

            assert expectedTasks == actualTasks
            return this
        }

        ExecutionResult assertTasksNotSkipped(Object... taskPaths) {
            def expectedTasks = taskPaths.flatten() as Set
            def tasks = tasks.findAll { !(it.outcome in SKIPPED_TASK_OUTCOMES) }.collect { it.path } as Set

            assert expectedTasks == tasks
            return this
        }
    }
}
