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

class BasePluginFunctionalTest extends AbstractFunctionalTest {

    def "adds Asciidoctor attributes for samples code and output directory"() {
        given:
        buildFile << """
            task verifyAsciidoctorAttributes {
                doLast {
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
