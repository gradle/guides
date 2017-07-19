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
}
