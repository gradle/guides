package org.gradle.guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.notNullValue
import static org.junit.Assume.assumeThat

class SamplesTest extends AbstractSamplesFunctionalTest {

    def setup() {
        initialize()
    }

    @Unroll
    def "#section/#sample with #dsl android build"() {

        assumeThat(System.getenv('ANDROID_HOME'), notNullValue())

        setup:
        copySampleCode("../$section/$sample")

        expect:
        succeeds("help")

        where:
        section               | sample                | dsl
        "declarative-scripts" | "declarative-android" | "kotlin"
        "declarative-scripts" | "imperative-android"  | "kotlin"
    }
}
