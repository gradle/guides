package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

class BasicExplicitBothDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                create("demo") {
                    sampleDir = file('src')
                    
                    withGroovyDsl()
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
- link:{zip-base-file-name}-groovy-dsl.zip[Download Groovy DSL ZIP]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeGroovyDslSample("src")
        writeKotlinDslSample("src")
    }

    @Override
    protected List<File> getDslZipFiles(Map m) {
        return [getGroovyDslZipFile(m), getKotlinDslZipFile(m)]
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertBothDslSampleTasksExecutedAndNotSkipped(result);
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
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle")
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")
    }

    @Override
    protected void assertDslZipFilesExists(Map m) {
        assert getGroovyDslZipFile(m).exists()
        assert getKotlinDslZipFile(m).exists()
    }

    @Override
    protected void assertDslZipFilesDoesNotExists(Map m) {
        assert !getGroovyDslZipFile(m).exists()
        assert !getKotlinDslZipFile(m).exists()
    }

    // TODO: Calling multiple time withGroovyDsl and withKotlinDsl is allowed

    def "can relocate both DSL sample source"() {
        given:
        buildFile << """
plugins {
    id("org.gradle.samples")
}

samples {
    demo {
        sampleDir = file('src')
        withGroovyDsl {
            archiveContent.from(file('src/groovy-dsl'))
        }
        withKotlinDsl {
            archiveContent.from(file('src/kotlin-dsl'))
        }
    }
}
"""
        writeSampleContent()
        writeGroovyDslSampleToDirectory('src/groovy-dsl')
        temporaryFolder.newFolder('src', 'groovy')
        temporaryFolder.newFile('src/groovy/do.not.include')

        writeKotlinDslSampleToDirectory('src/kotlin-dsl')
        temporaryFolder.newFolder('src', 'kotlin')
        temporaryFolder.newFile('src/kotlin/do.not.include')

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHasContent()
    }
}
