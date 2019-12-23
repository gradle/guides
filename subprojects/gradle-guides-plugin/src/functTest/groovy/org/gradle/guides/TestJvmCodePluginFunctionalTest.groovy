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

class TestJvmCodePluginFunctionalTest extends AbstractFunctionalTest {

    def setup() {
        buildFile << """
            plugins {
                id 'org.gradle.guides.test-jvm-code'
            }
        """
    }

    def "sets sample directory system property for test task"() {
        buildFile << """
            task verifyTestSampleDirSystemProperty {
                doLast {
                    test.systemProperties['sampleDir'] == file('samples')
                }
            }
        """

        when:
        build('verifyTestSampleDirSystemProperty')

        then:
        noExceptionThrown()
    }

    def "can use sample directory system property in test class"() {
        def testDir = temporaryFolder.newFolder('src', 'test', 'groovy')
        temporaryFolder.newFolder('samples')
        new File(testDir, 'MyTest.groovy') << """
import spock.lang.Specification

class MyTest extends Specification {
    def "can resolve samplesDir system property"() {
        expect:
        def samplesDir = System.properties['samplesDir']
        samplesDir
        samplesDir.endsWith('samples')
    }
}
"""

        when:
        build('test')

        then:
        noExceptionThrown()
    }

    def "can use samples functional test fixtures"() {
        def testDir = temporaryFolder.newFolder('src', 'test', 'groovy')
        new File(testDir, 'MyTest.groovy') << """
            import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
            
            class MyTest extends AbstractSamplesFunctionalTest {
                def "can execute hello world sample"() {
                    given:
                    copySampleCode('hello-world')
                
                    when:
                    def result = succeeds('helloWorld')
                
                    then:
                    result.output.contains('Hello world')
                }
            }
        """
        def samplesCodeDir = temporaryFolder.newFolder('samples', 'code', 'hello-world')
        new File(samplesCodeDir, 'build.gradle') << """
            task helloWorld {
                doLast {
                    println 'Hello world'
                }
            }
        """

        when:
        build('test')

        then:
        noExceptionThrown()
    }

    def "test is out of date if samples change"() {
        def testDir = temporaryFolder.newFolder('src', 'test', 'groovy')
        new File(testDir, 'MyTest.groovy') << """
import spock.lang.Specification

class MyTest extends Specification {
    def "always passes"() {
        expect:
        true
    }
}
"""
        def samplesCodeDir = temporaryFolder.newFolder('samples', 'code', 'hello-world')
        def sampleCode = new File(samplesCodeDir, 'build.gradle')
        sampleCode << """
            task helloWorld {
                doLast {
                    println 'Hello world'
                }
            }
        """

        when:
        def result = build("test")

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        sampleCode << 'task otherTask {}'
        result = build("test")

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        result = build("test")

        then:
        result.task(":test").outcome == TaskOutcome.UP_TO_DATE
    }
}
