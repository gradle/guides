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
        section                           | sample                            | ext
        "applying-plugins"                | "declarative"                     | ".gradle"
        "applying-plugins"                | "declarative"                     | ".gradle.kts"
        "applying-plugins"                | "imperative"                      | ".gradle"
        "applying-plugins"                | "imperative"                      | ".gradle.kts"
        "configuring-plugins"             | "extensions"                      | ".gradle"
        "configuring-plugins"             | "extensions"                      | ".gradle.kts"
        "configuring-tasks"               | "basics"                          | ".gradle"
        "configuring-tasks"               | "basics"                          | ".gradle.kts"
        "configuring-tasks"               | "spring-boot"                     | ".gradle"
        "configuring-tasks"               | "spring-boot"                     | ".gradle.kts"
        "creating-tasks"                  | "task-container"                  | ".gradle"
        "creating-tasks"                  | "task-container"                  | ".gradle.kts"
        "creating-tasks"                  | "project-api"                     | ".gradle"
        "creating-tasks"                  | "project-api"                     | ".gradle.kts"
        "creating-tasks"                  | "reference"                       | ".gradle"
        "creating-tasks"                  | "reference"                       | ".gradle.kts"
        "configurations-and-dependencies" | "declarative"                     | ".gradle"
        "configurations-and-dependencies" | "declarative"                     | ".gradle.kts"
        "configurations-and-dependencies" | "imperative"                      | ".gradle"
        "configurations-and-dependencies" | "imperative-string-reference"     | ".gradle.kts"
        "configurations-and-dependencies" | "imperative-delegated-properties" | ".gradle.kts"
        "configurations-and-dependencies" | "custom"                          | ".gradle"
        "configurations-and-dependencies" | "custom"                          | ".gradle.kts"
        "interoperability"                | "closureOf"                       | ".gradle.kts"
        "interoperability"                | "delegateClosureOf"               | ".gradle.kts"
        "interoperability"                | "withGroovyBuilder"               | ".gradle.kts"
    }

    @Unroll
    def "#section/#sample with #dsl build"() {

        setup:
        copySampleCode("$section/$sample")

        expect:
        succeeds("help")

        where:
        section                | sample              | dsl
        "multi-project-builds" | "groovy-dsl"        | "groovy"
        "multi-project-builds" | "kotlin-dsl"        | "kotlin"
        "interoperability"     | "static-extensions" | "mixed"
    }

    @Unroll
    def "#section/#sample with #dsl android build"() {

        assumeThat(System.getenv('ANDROID_HOME'), notNullValue())

        setup:
        copySampleCode("$section/$sample")

        expect:
        succeeds("help")

        where:
        section               | sample                | dsl
        "declarative-scripts" | "declarative-android" | "groovy"
        "declarative-scripts" | "imperative-android"  | "kotlin"
    }
}
