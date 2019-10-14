package org.gradle.samples

class TestWithExemplarExplicitBothDslSampleFunctionalTest extends AbstractTestWithExemplarSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples.all {
                withGroovyDsl()
                withKotlinDsl()
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
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeGroovyDslSample(directory)
        writeKotlinDslSample(directory)
    }

    @Override
    protected String getExemplarSampleConfigFileContent() {
        return """
commands: [{
    execution-subdirectory: groovy
    executable: gradle
    args: tasks
},{
    execution-subdirectory: kotlin
    executable: gradle
    args: tasks
}]
"""
    }
}
