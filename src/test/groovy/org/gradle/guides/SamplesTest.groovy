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
    def "#section/#sample with #ext script"() {

        setup:
        copySampleCode("$section")

        expect:
        succeeds("help", "-b", "$sample$ext")

        where:
        section               | sample        | ext
        "applying-plugins"    | "declarative" | ".gradle"
        "applying-plugins"    | "declarative" | ".gradle.kts"
        "applying-plugins"    | "imperative"  | ".gradle"
        "applying-plugins"    | "imperative"  | ".gradle.kts"
        "configuring-plugins" | "extensions"  | ".gradle"
        "configuring-plugins" | "extensions"  | ".gradle.kts"
        "configuring-tasks"   | "spring-boot" | ".gradle"
        "configuring-tasks"   | "spring-boot" | ".gradle.kts"
    }

    @Unroll
    def "#section/#sample with #dsl android build"() {

        setup:
        copySampleCode("$section/$sample")
        assumeThat(System.getenv('ANDROID_HOME'), notNullValue())

        expect:
        succeeds("help")

        where:
        section               | sample                | dsl
        "declarative-scripts" | "declarative-android" | "groovy"
        "declarative-scripts" | "imperative-android"  | "kotlin"
    }
}
