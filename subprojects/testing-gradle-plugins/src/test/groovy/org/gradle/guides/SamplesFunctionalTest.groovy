package org.gradle.guides

import spock.lang.Unroll

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    def "can use composite builds"() {
        given:
        copySampleCode('include-plugin-build', 'consumer')
        copySampleCode('url-verifier-plugin', 'url-verifier-plugin')
        
        when:
        def result = gradleRunner.withProjectDir(new File(testDirectory, 'consumer')).withArguments('verifyUrl').build()
        
        then:
        result.task(':verifyUrl').outcome == SUCCESS
        result.output.contains("Successfully resolved URL 'https://www.google.com/'")
    }

    def "can execute all tests in plugin project"() {
        given:
        copySampleCode('url-verifier-plugin')
        
        when:
        def result = succeeds('check')
        
        then:
        result.task(':test').outcome == SUCCESS
        result.task(':integrationTest').outcome == SUCCESS
        result.task(':functionalTest').outcome == SUCCESS
    }
}