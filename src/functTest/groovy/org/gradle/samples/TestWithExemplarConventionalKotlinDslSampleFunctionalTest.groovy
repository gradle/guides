package org.gradle.samples

import spock.lang.Ignore

@Ignore
class TestWithExemplarConventionalKotlinDslSampleFunctionalTest extends AbstractTestWithExemplarSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                demo
            }
        """
    }

    @Override
    protected void writeSampleUnderTestToDirectory(String directory) {
        writeReadmeTo(file(directory)) << """
ifndef::env-github[]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeKotlinDslSample(directory)
    }

    @Override
    protected String getExemplarSampleConfigFileContent() {
        return """
commands: [{
    execution-subdirectory: kotlin
    executable: gradle
    args: help
    expected-output-file: showDemoSample.sample.out
}]
"""
    }
}
