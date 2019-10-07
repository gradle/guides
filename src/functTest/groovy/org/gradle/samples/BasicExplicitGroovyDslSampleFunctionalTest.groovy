package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

class BasicExplicitGroovyDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                demo {
                    withGroovyDsl()
                }
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
        writeKotlinDslSample(directory)
    }

    @Override
    protected List<File> getDslZipFiles(Map m) {
        return [getGroovyDslZipFile(m)]
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyGroovyDslTasksExecutedAndNotSkipped(result);
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
        assert sampleIndexFile.text.contains("""<a href="demo${version}-groovy-dsl.zip">""")
        assert !sampleIndexFile.text.contains("""<a href="demo${version}-kotlin-dsl.zip">""")
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
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle")
        assert !kotlinDslZipFile.exists()
    }

    @Override
    protected void assertDslZipFilesExists(Map m) {
        assert getGroovyDslZipFile(m).exists()
        assert !getKotlinDslZipFile(m).exists()
    }

    @Override
    protected void assertDslZipFilesDoesNotExists(Map m) {
        assert !getGroovyDslZipFile(m).exists()
        assert !getKotlinDslZipFile(m).exists()
    }

    def "only contains Groovy DSL sample even if Kotlin DSL source are available"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeGroovyDslSample('src/demo')

        when:
        def result = build('assembleDemoSample')

        then:
        assertOnlyGroovyDslTasksExecutedAndNotSkipped(result)
        groovyDslZipFile.exists()
        !kotlinDslZipFile.exists()
    }

    // TODO: Calling multiple time withGroovyDsl is allowed

    def "can relocate Groovy DSL sample source"() {
        given:
        buildFile << """
plugins {
    id("org.gradle.samples")
}

samples {
    demo {
        sampleDirectory = file('src')
        withGroovyDsl {
            archiveContent.from(file('src/groovy-dsl'))
        }
    }
}
"""
        writeSampleContentToDirectory('src')
        writeGroovyDslSampleToDirectory('src/groovy-dsl')
        temporaryFolder.newFolder('src', 'groovy')
        temporaryFolder.newFile('src/groovy/do.not.include')

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHasContent()
    }
}
