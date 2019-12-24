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

package org.gradle.docs.guides


import spock.lang.Unroll

abstract class AbstractGuidesPluginFunctionalTest extends AbstractFunctionalTest {
    abstract String getPluginId()

    def setup() {
        buildFile << """
            plugins {
                id '${pluginId}'
            }
        """
        createContentsDir()
    }

    @Unroll
    def "builds the guide using task #task"() {
        given:
        asciidocSourceFile << """
A simple guide
"""
        // Should not need to create this
        temporaryFolder.newFolder("samples")

        when:
        build(task)

        then:
        def htmlFile = new File(temporaryFolder.root, 'build/html5/index.html')
        htmlFile.exists()

        where:
        task << ["asciidoctor", "assemble"]
    }

    File getAsciidocSourceFile() {
        return temporaryFolder.newFile("contents/index.adoc")
    }

    File createSamplesCodeDir() {
        temporaryFolder.newFolder('samples', 'code')
    }

    File createSamplesOutputDir() {
        temporaryFolder.newFolder('samples', 'output')
    }

    File createContentsDir() {
        temporaryFolder.newFolder('contents')
    }
}
