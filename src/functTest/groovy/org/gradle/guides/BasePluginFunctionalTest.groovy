/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.guides

import org.gradle.testkit.runner.TaskOutcome

class BasePluginFunctionalTest extends AbstractFunctionalTest {

    def "adds Asciidoctor attributes for samples code and output directory"() {
        given:
        buildFile << """
            task verifyAsciidoctorAttributes {
                doLast {
                    assert asciidoctor.attributes['samples-dir'] == file('samples')
                    assert asciidoctor.attributes['samplescodedir'] == file('samples/code')
                    assert asciidoctor.attributes['samplesoutputdir'] == file('samples/output')
                }
            }
        """

        when:
        build('verifyAsciidoctorAttributes')

        then:
        noExceptionThrown()
    }

    def "can reference attributes for samples directories in Asciidoc generation"() {
        given:
        def contentsDir = temporaryFolder.newFolder('contents')
        new File(contentsDir, 'index.adoc') << """
My build file:
include::{samplescodedir}/helloWorld/build.gradle[]
Output:
include::{samplesoutputdir}/helloWorld/build.out[]
"""
        def samplesCodeFolder = createSamplesCodeDir()
        File codeDir = createDir(samplesCodeFolder, 'helloWorld')
        new File(codeDir, 'build.gradle') << """
task helloWorld {
    doLast {
        println 'Hello world!'
    }
}
"""
        def samplesOutputFolder = createSamplesOutputDir()
        File outputDir = createDir(samplesOutputFolder, 'helloWorld')
        new File(outputDir, 'build.out') << """
> Task :helloWorld
Hello world!
"""

        when:
        build('asciidoctor')

        then:
        def htmlFile = new File(temporaryFolder.root, 'build/html5/index.html').text
        htmlFile.contains('task helloWorld')
        htmlFile.contains('Task :helloWorld')
    }

    def "referenced, non-existent samples directories are flagged as unresolved directive in Asciidoc generation"() {
        given:
        def contentsDir = createContentsDir()
        new File(contentsDir, 'index.adoc') << """
My build file:
include::{samplescodedir}/helloWorld/build.gradle[]
Output:
include::{samplesoutputdir}/helloWorld/build.out[]
"""

        when:
        build('asciidoctor')

        then:
        def htmlFile = new File(temporaryFolder.root, 'build/html5/index.html').text
        htmlFile.contains("Unresolved directive in index.adoc - include::${temporaryFolder.root.canonicalPath}/samples/code/helloWorld/build.gradle[]")
        htmlFile.contains("Unresolved directive in index.adoc - include::${temporaryFolder.root.canonicalPath}/samples/output/helloWorld/build.out[]")
    }

    def "asciidoctor is out of date if samples change"() {
        given:
        def asciiDoctorTask = ":asciidoctor"
        def contentsDir = createContentsDir()
        new File(contentsDir, "index.adoc") << 'This is some sample ascii source'
        def samplesCodeDir = createSamplesCodeDir()
        def samplesOutputDir = createSamplesOutputDir()

        when:
        def result = build(asciiDoctorTask)

        then:
        result.task(asciiDoctorTask).outcome == TaskOutcome.SUCCESS

        when:
        new File(samplesCodeDir, "build.gradle") << 'apply plugin: java'
        result = build(asciiDoctorTask)

        then:
        result.task(asciiDoctorTask).outcome == TaskOutcome.SUCCESS

        when:
        new File(samplesOutputDir, "my-task-output.log") << 'Build SUCCESSFUL'
        result = build(asciiDoctorTask)

        then:
        result.task(asciiDoctorTask).outcome == TaskOutcome.SUCCESS

        when:
        result = build(asciiDoctorTask, '--info')

        then:
        result.task(asciiDoctorTask).outcome == TaskOutcome.UP_TO_DATE
    }

    def "header and footer is injected during asciidoctor postprocessing"() {
        given:
        def asciiDoctorTask = ":asciidoctor"
        def contentsDir = createContentsDir()
        new File(contentsDir, "index.adoc") << 'This is some sample ascii source'

        when:
        build(asciiDoctorTask)

        then:
        def htmlFile = new File(temporaryFolder.root, 'build/html5/index.html').text
        htmlFile.contains('<script defer src="https://guides.gradle.org/js/guides')
        htmlFile.contains('<header class="site-layout__header site-header" itemscope="itemscope" itemtype="https://schema.org/WPHeader">')
        htmlFile.contains('<footer class="site-layout__footer site-footer" itemscope="itemscope" itemtype="https://schema.org/WPFooter">')
    }

    private File createSamplesCodeDir() {
        temporaryFolder.newFolder('samples', 'code')
    }

    private File createSamplesOutputDir() {
        temporaryFolder.newFolder('samples', 'output')
    }

    private File createContentsDir() {
        temporaryFolder.newFolder('contents')
    }
}
