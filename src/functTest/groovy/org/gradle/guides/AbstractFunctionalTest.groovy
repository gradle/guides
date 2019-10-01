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

package org.gradle.guides

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractFunctionalTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    File projectDir
    File buildFile
    File settingsFile
    private String gradleVersion

    def setup() {
        projectDir = temporaryFolder.root
        buildFile = temporaryFolder.newFile('build.gradle')
        settingsFile = temporaryFolder.newFile('settings.gradle')
        new File(projectDir, "gradle.properties").text = "org.gradle.jvmargs=-XX:MaxMetaspaceSize=500m -Xmx500m"
    }

    protected BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        createAndConfigureGradleRunner(arguments).buildAndFail()
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        def allArgs = (arguments as List) + ["-S"]
        def runner = GradleRunner.create().withProjectDir(projectDir).withArguments(allArgs).withPluginClasspath().forwardOutput()
        if (gradleVersion != null) {
            runner.withGradleVersion(gradleVersion)
            gradleVersion = null
        }
        return runner
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
}
