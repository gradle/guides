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

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AbstractSampleFunctionalSpec extends AbstractFunctionalTest {
    protected void writeGroovyDslSample(String sampleDirectory) {
        def directory = "${sampleDirectory}/groovy"
        temporaryFolder.newFolder(directory.split('/'))
        temporaryFolder.newFile("${directory}/build.gradle") << """
            println "Hello, world!"
        """
        temporaryFolder.newFile("${directory}/settings.gradle") << """
            rootProject.name = 'demo'
        """
    }

    protected void writeKotlinDslSample(String sampleDirectory) {
        def directory = "${sampleDirectory}/kotlin"
        temporaryFolder.newFolder(directory.split('/'))
        temporaryFolder.newFile("${directory}/build.gradle.kts") << """
            println("Hello, world!")
        """
        temporaryFolder.newFile("${directory}/settings.gradle.kts") << """
            rootProject.name = "demo"
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

    protected static void assertBothDslSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateWrapperForDemoSample").outcome == SUCCESS
        assert result.task(":syncDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":syncDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":compressDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":compressDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":assembleDemoSample").outcome == SUCCESS
    }

    protected static void assertOnlyGroovyDslTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateWrapperForDemoSample").outcome == SUCCESS
        assert result.task(":syncDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":syncDemoKotlinDslSample") == null
        assert result.task(":compressDemoGroovyDslSample").outcome == SUCCESS
        assert result.task(":compressDemoKotlinDslSample") == null
        assert result.task(":assembleDemoSample").outcome == SUCCESS
    }

    protected static void assertOnlyKotlinDslTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(":generateWrapperForDemoSample").outcome == SUCCESS
        assert result.task(":syncDemoGroovyDslSample") == null
        assert result.task(":syncDemoKotlinDslSample").outcome == SUCCESS
        assert result.task(":compressDemoGroovyDslSample") == null
        assert result.task(":compressDemoKotlinDslSample").outcome == SUCCESS
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
}
