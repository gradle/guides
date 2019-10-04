/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.samples

import org.gradle.guides.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AbstractSampleFunctionalTest extends AbstractFunctionalTest {
    protected void writeSampleUnderTest() {
        temporaryFolder.newFolder("src")
        temporaryFolder.newFile("src/README.adoc") << """
= Demo Sample

Some doc

ifndef::env-github[]
- link:{zip-base-file-name}-groovy-dsl.zip[Download Groovy DSL ZIP]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        temporaryFolder.newFolder("src", "groovy")
        temporaryFolder.newFile("src/groovy/build.gradle") << """
            println "Hello, world!"
        """
        temporaryFolder.newFile("src/groovy/settings.gradle") << """
            rootProject.name = 'demo'
        """
        temporaryFolder.newFolder("src", "kotlin")
        temporaryFolder.newFile("src/kotlin/build.gradle.kts") << """
            println("Hello, world!")
        """
        temporaryFolder.newFile("src/kotlin/settings.gradle.kts") << """
            rootProject.name = "demo"
        """
    }

    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                create("demo") {
                    sampleDir = file('src')
                }
            }
        """
    }

    protected File getGroovyDslZipFile(Map m = [:]) {
        return getDslZipFile(m + [dsl: 'groovy-dsl'])
    }

    protected File getKotlinDslZipFile(Map m = [:]) {
        return getDslZipFile(m + [dsl: 'kotlin-dsl'])
    }

    private File getDslZipFile(Map m) {
        def versionToken = m.version ? "${m.version}-" : ''
        def buildDirectoryRelativePathToken = m.getOrDefault('buildDirectoryRelativePath', 'gradle-samples')
        def dslToken = m.dsl
        return new File(projectDir, "build/${buildDirectoryRelativePathToken}/demo/demo-${versionToken}${dslToken}.zip")
    }

    protected File getSampleReadMeFile() {
        return new File(projectDir, "src/README.adoc")
    }

    protected String getSampleUnderTestDsl() {
        return "samples.demo"
    }

    protected static void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateWrapperForDemoSample").outcome == SUCCESS
        assert result.task(":syncDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":syncDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":compressDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":compressDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":assembleDemoSample").outcome == SUCCESS
    }
}
