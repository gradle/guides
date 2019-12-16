package org.gradle.guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

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

    def "can document Kotlin sources"() {
        given:
        copySampleCode('step-3')

        when:
        def result = succeeds('dokkaJar')

        then:
        result.task(':dokka').outcome == SUCCESS
        result.task(':dokkaJar').outcome == SUCCESS
    }

    def "can publish Kotlin library Jars"() {
        given:
        copySampleCode('step-4')

        when:
        def result = succeeds('publish')

        then:
        result.task(':jar').outcome == SUCCESS
        result.task(':dokkaJar').outcome == SUCCESS
        result.task(':publish').outcome == SUCCESS
    }
}
