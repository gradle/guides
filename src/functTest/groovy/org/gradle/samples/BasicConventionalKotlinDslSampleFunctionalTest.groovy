package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

class BasicConventionalKotlinDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
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
        writeSampleContentToDirectory(directory) << """
ifndef::env-github[]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeKotlinDslSample(directory)
    }

    @Override
    protected List<File> getDslZipFiles(Map m) {
        return [getKotlinDslZipFile(m)]
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyKotlinDslTasksExecutedAndNotSkipped(result);
    }

    @Override
    protected void assertSampleIndexContainsLinkToSampleArchives(String version) {
        if (version == null) {
            version = ''
        } else {
            version = "-${version}"
        }

        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        assert sampleIndexFile.exists()
        assert !sampleIndexFile.text.contains("""<a href="demo${version}-groovy-dsl.zip">""")
        assert sampleIndexFile.text.contains("""<a href="demo${version}-kotlin-dsl.zip">""")
    }

    @Override
    protected void assertSampleIndexDoesNotContainsLinkToSampleArchives(String version) {
        if (version == null) {
            version = ''
        } else {
            version = "-${version}"
        }

        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        assert sampleIndexFile.exists()
        assert !sampleIndexFile.text.contains("""<a href="demo${version}-groovy-dsl.zip">""")
        assert !sampleIndexFile.text.contains("""<a href="demo${version}-kotlin-dsl.zip">""")
    }

    @Override
    protected void assertDslZipsHasContent() {
        assert !groovyDslZipFile.exists()
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")
    }

    @Override
    protected void assertDslZipFilesExists(Map m) {
        assert !getGroovyDslZipFile(m).exists()
        assert getKotlinDslZipFile(m).exists()
    }

    @Override
    protected void assertDslZipFilesDoesNotExists(Map m) {
        assert !getGroovyDslZipFile(m).exists()
        assert !getKotlinDslZipFile(m).exists()
    }
}
