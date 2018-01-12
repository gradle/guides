package org.gradle.guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import org.junit.Ignore

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    def "can compile Kotlin sources"() {
        given:
        copySampleCode('step-1')

        when:
        def result = succeeds('compileKotlin')

        then:
        result.task(':compileKotlin').outcome == SUCCESS
    }

    def "can run Kotlin tests"() {
        given:
        copySampleCode('step-2')

        when:
        def result = succeeds('test')

        then:
        result.task(':test').outcome == SUCCESS
    }
}
