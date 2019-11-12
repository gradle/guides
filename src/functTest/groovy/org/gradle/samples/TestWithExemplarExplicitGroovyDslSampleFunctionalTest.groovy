package org.gradle.samples

class TestWithExemplarExplicitGroovyDslSampleFunctionalTest extends AbstractTestWithExemplarSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples.all {
                withGroovyDsl()
            }

            samples {
                demo
            }
        """
    }

    @Override
    protected void writeSampleUnderTestToDirectory(String directory) {
        writeSampleContentToDirectory(directory) << """
ifndef::env-github[]
- link:{zip-base-file-name}-groovy-dsl.zip[Download Groovy DSL ZIP]
endif::[]
"""
        writeGroovyDslSample(directory)
    }

    @Override
    protected String getExemplarSampleConfigFileContent() {
        return """
commands: [{
    execution-subdirectory: groovy
    executable: gradle
    args: help
    expected-output-file: showDemoSample.sample.out
}]
"""
    }
}
