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

import org.gradle.testkit.runner.TaskOutcome

class BasePluginFunctionalTest extends AbstractGuidesPluginFunctionalTest {
    String pluginId = "org.gradle.guides.base"

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
        asciidocSourceFile << """
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

    def "asciidoctor is out of date if samples change"() {
        given:
        def asciiDoctorTask = ":asciidoctor"
        asciidocSourceFile << 'This is some sample ascii source'
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
        createSamplesCodeDir()
        asciidocSourceFile << 'This is some sample ascii source'

        when:
        build(asciiDoctorTask)

        then:
        def htmlFile = new File(temporaryFolder.root, 'build/html5/index.html').text
        htmlFile.contains('<script defer src="https://guides.gradle.org/js/guides')
        htmlFile.contains('<header class="site-layout__header site-header js-site-header" itemscope="itemscope" itemtype="https://schema.org/WPHeader">')
        htmlFile.contains('<footer class="site-layout__footer site-footer" itemscope="itemscope" itemtype="https://schema.org/WPFooter">')
    }

    def "defaults to current Gradle version for minimum Gradle version of the guide"() {
        given:
        buildFile << """
            task verifyAsciidoctorAttributes {
                doLast {
                    assert asciidoctor.attributes['gradle-version'] == gradle.gradleVersion
                    assert asciidoctor.attributes['user-manual'] == "https://docs.gradle.org/\${gradle.gradleVersion}/userguide/".toString()
                    assert asciidoctor.attributes['language-reference'] == "https://docs.gradle.org/\${gradle.gradleVersion}/dsl/".toString()
                    assert asciidoctor.attributes['api-reference'] == "https://docs.gradle.org/\${gradle.gradleVersion}/javadoc/".toString()
                }
            }
        """

        when:
        build('verifyAsciidoctorAttributes')

        then:
        noExceptionThrown()
    }

    def "can configure the minimum Gradle version of the guide"() {
        given:
        buildFile << """
            guide {
                minimumGradleVersion = "5.2"
            }

            task verifyAsciidoctorAttributes {
                doLast {
                    assert asciidoctor.attributes['gradle-version'] == '5.2'
                    assert asciidoctor.attributes['user-manual'] == 'https://docs.gradle.org/5.2/userguide/'
                    assert asciidoctor.attributes['language-reference'] == 'https://docs.gradle.org/5.2/dsl/'
                    assert asciidoctor.attributes['api-reference'] == 'https://docs.gradle.org/5.2/javadoc/'
                }
            }
        """

        when:
        build('verifyAsciidoctorAttributes')

        then:
        noExceptionThrown()
    }

    def "can configure the title of the guide"() {
        given:
        buildFile << """
            guide {
                title = "Some Title"
            }

            task verifyAsciidoctorAttributes {
                doLast {
                    assert asciidoctor.attributes['guide-title'] == 'Some Title'
                }
            }
        """

        when:
        build('verifyAsciidoctorAttributes')

        then:
        noExceptionThrown()
    }

    def "can configure the repository path of the guide"() {
        given:
        buildFile << """
            guide {
                repositoryPath = "foo/bar"
            }

            task verifyAsciidoctorAttributes {
                doLast {
                    assert asciidoctor.attributes['repo-path'] == 'foo/bar'
                    assert asciidoctor.attributes['repository-path'] == 'foo/bar'
                }
            }
        """

        when:
        build('verifyAsciidoctorAttributes')

        then:
        noExceptionThrown()
    }

    def "defaults guide title to project name as title case"() {
        given:
        buildFile << """
            task verifyGuideTitle {
                doLast {
                    assert guide.title.get() == 'Some Project Name'
                }
            }
        """
        settingsFile << "rootProject.name = 'some-project-name'"

        when:
        build('verifyGuideTitle')

        then:
        noExceptionThrown()
    }

    def "defaults guide repository path to project name under gradle-guides organization"() {
        given:
        buildFile << """
            task verifyGuideRepositoryPath {
                doLast {
                    assert guide.repositoryPath.get() == 'gradle-guides/some-project-name'
                }
            }
        """
        settingsFile << "rootProject.name = 'some-project-name'"

        when:
        build('verifyGuideRepositoryPath')

        then:
        noExceptionThrown()
    }

    def "defaults guide description to guide title"() {
        given:
        buildFile << """
            task verifyGuideDescription {
                doLast {
                    assert guide.description.get() == 'Some Project Name'
                    assert guide.description.get() == guide.title.get()

                    guide.title.set('Some OTHER Project Name')
                    assert guide.description.get() == 'Some OTHER Project Name'

                    guide.description.set('Some description')
                    assert guide.description.get() == 'Some description'
                }
            }
        """
        settingsFile << "rootProject.name = 'some-project-name'"

        when:
        build('verifyGuideDescription')

        then:
        noExceptionThrown()
    }
}
