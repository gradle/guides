package org.gradle.guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {
    @Unroll
    def "can execute incremental task sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/incremental-task")

        when:
        succeeds('generate')

        then:
        def outputDir = new File(testDirectory, 'build/generated-output')
        new File(outputDir, '1.txt').text == 'Hello World!'
        new File(outputDir, '2.txt').text == 'Hello World!'

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
    }
}
