package org.gradle.guides

import spock.lang.Unroll

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    def "can execute all tests in plugin project"() {
        given:
        copySampleCode('url-verifier-plugin')
        
        when:
        succeeds('test')
        
        then:
        noExceptionThrown()
    }
}