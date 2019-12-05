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
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.hamcrest.CoreMatchers.containsString

class AbstractSampleFunctionalSpec extends AbstractFunctionalTest {
    protected File writeSampleContentToDirectory(String directory) {
        temporaryFolder.newFolder(directory.split('/'))
        return temporaryFolder.newFile("${directory}/README.adoc") << """
= Demo Sample

Some doc
"""
    }

    protected void writeGroovyDslSample(String sampleDirectory) {
        writeGroovyDslSampleToDirectory("${sampleDirectory}/groovy")
    }

    protected void writeGroovyDslSampleToDirectory(String directory) {
        temporaryFolder.newFolder(directory.split('/'))
        temporaryFolder.newFile("${directory}/build.gradle") << """
// tag::println[]
println "Hello, world!"
// end:println[]
"""
        temporaryFolder.newFile("${directory}/settings.gradle") << """
// tag::root-project-name[]
rootProject.name = 'demo'
// end:root-project-name[]
"""
    }

    protected void writeKotlinDslSample(String sampleDirectory) {
        writeKotlinDslSampleToDirectory("${sampleDirectory}/kotlin")
    }

    protected void writeKotlinDslSampleToDirectory(String directory) {
        temporaryFolder.newFolder(directory.split('/'))
        temporaryFolder.newFile("${directory}/build.gradle.kts") << """
// tag::println[]
println("Hello, world!")
// end:println[]
        """
        temporaryFolder.newFile("${directory}/settings.gradle.kts") << """
// tag::root-project-name[]
rootProject.name = "demo"
// end:root-project-name[]
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
        def name = m.name ? m.name : 'demo'
        def baseName = name.capitalize()
        def buildDirectoryRelativePathToken = m.getOrDefault('buildDirectoryRelativePath', 'gradle-samples')
        def dslToken = m.dsl
        return new File(projectDir, "build/${buildDirectoryRelativePathToken}/${name}/${baseName}-${versionToken}${dslToken}.zip")
    }

    protected File getSampleReadMeFile() {
        return new File(projectDir, "src/samples/demo/README.adoc")
    }

    protected String getSampleUnderTestDsl() {
        return "samples.demo"
    }

    protected static void assertBothDslSampleTasksExecutedAndNotSkipped(BuildResult result, String name = 'demo') {
        assert result.task(":generateWrapperFor${name.capitalize()}Sample").outcome == SUCCESS
        assert result.task(":install${name.capitalize()}GroovyDslSample").outcome == SUCCESS
        assert result.task(":install${name.capitalize()}KotlinDslSample").outcome == SUCCESS
        assert result.task(":compress${name.capitalize()}GroovyDslSample").outcome == SUCCESS
        assert result.task(":compress${name.capitalize()}KotlinDslSample").outcome == SUCCESS
        assert result.task(":install${name.capitalize()}Sample").outcome == SUCCESS
        assert result.task(":assemble${name.capitalize()}Sample").outcome == SUCCESS
    }

    protected static void assertOnlyGroovyDslTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateWrapperForDemoSample").outcome == SUCCESS
        assert result.task(":installDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":installDemoKotlinDslSample") == null
        assert result.task(":compressDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":compressDemoKotlinDslSample") == null
        assert result.task(":installDemoSample").outcome == SUCCESS
        assert result.task(":assembleDemoSample").outcome == SUCCESS
    }

    protected static void assertOnlyKotlinDslTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateWrapperForDemoSample").outcome == SUCCESS
        assert result.task(":installDemoGroovyDslSample") == null
        assert result.task(":installDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":compressDemoGroovyDslSample") == null
        assert result.task(":compressDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":installDemoSample").outcome == SUCCESS
        assert result.task(":assembleDemoSample").outcome == SUCCESS
    }

    protected static void assertZipHasContent(File file, String... expectedContent) {
        assert file.exists()
        def content = new ZipFile(file).withCloseable { zipFile ->
            return zipFile.entries().findAll { !it.directory }.collect { ZipEntry zipEntry ->
                return zipEntry.getName()
            }
        } as Set

        assert content.size() == expectedContent.size()
        content.removeAll(Arrays.asList(expectedContent))
        assert content.empty
    }

    protected static void assertGradleWrapperVersion(File file, String expectedGradleVersion) {
        assertFileInZipThat(file, 'gradle/wrapper/gradle-wrapper.properties', containsString("-${expectedGradleVersion}-"))
    }

    protected static void assertFileInZipThat(File file, String path, Matcher<String> matcher) {
        assert file.exists()
        def text = new ZipFile(file).withCloseable { zipFile ->
            return zipFile.getInputStream(zipFile.entries().findAll { !it.directory }.find { it.name == path }).text
        }

        MatcherAssert.assertThat(text, matcher)
    }
}
