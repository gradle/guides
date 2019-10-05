package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

class BasicExplicitKotlinDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                create("demo") {
                    sampleDir = file('src')
                    
                    withKotlinDsl()
                }
            }
        """
    }

    @Override
    protected void writeSampleUnderTest() {
        temporaryFolder.newFolder("src")
        temporaryFolder.newFile("src/README.adoc") << """
= Demo Sample

Some doc

ifndef::env-github[]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeGroovyDslSample("src")
        writeKotlinDslSample("src")
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyKotlinDslTasksExecutedAndNotSkipped(result);
    }

    @Override
    protected void assertSampleIndexContainsLinkToSampleArchives() {
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        assert sampleIndexFile.exists()
        assert sampleIndexFile.text.contains('<a href="demo-kotlin-dsl.zip">')
    }

    @Override
    protected void assertZipsHasContent() {
        assert !groovyDslZipFile.exists()
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")
    }
}
