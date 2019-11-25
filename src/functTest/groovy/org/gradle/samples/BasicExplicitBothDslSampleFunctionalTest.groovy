package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

import static org.hamcrest.CoreMatchers.not

class BasicExplicitBothDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
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
        writeSampleContentToDirectory(file(directory)) << """
ifndef::env-github[]
- link:{zip-base-file-name}-groovy-dsl.zip[Download Groovy DSL ZIP]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeGroovyDslSample(directory)
        writeKotlinDslSample(directory)
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
    protected void assertDslZipsHasContent(String... additionalFiles) {
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle", *additionalFiles)
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts", *additionalFiles)
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

    @Override
    protected boolean hasGroovyDsl() {
        return true
    }

    @Override
    protected boolean hasKotlinDsl() {
        return true
    }

    @Override
    protected void assertDslZipFilesDoesNotContainsAsciidoctorTags() {
        assertFileInZipThat(groovyDslZipFile, "build.gradle", not(containsAsciidoctorTags()))
        assertFileInZipThat(groovyDslZipFile, "settings.gradle", not(containsAsciidoctorTags()))
        assertFileInZipThat(kotlinDslZipFile, "build.gradle.kts", not(containsAsciidoctorTags()))
        assertFileInZipThat(kotlinDslZipFile, "settings.gradle.kts", not(containsAsciidoctorTags()))
    }

    @Override
    protected String useAsciidoctorSampleExtension() {
        return """
====
include::sample[dir="groovy",files="build.gradle[]"]
include::sample[dir="kotlin",files="build.gradle.kts[]"]
====
"""
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
        sampleDirectory = file('src')
        withGroovyDsl {
            archiveContent.from(file('src/groovy-dsl'))
        }
        withKotlinDsl {
            archiveContent.from(file('src/kotlin-dsl'))
        }
    }
}
"""
        writeSampleContentToDirectory(file('src'))
        writeGroovyDslSampleToDirectory(file('src/groovy-dsl'))
        temporaryFolder.newFolder('src', 'groovy')
        temporaryFolder.newFile('src/groovy/do.not.include')

        writeKotlinDslSampleToDirectory(file('src/kotlin-dsl'))
        temporaryFolder.newFolder('src', 'kotlin')
        temporaryFolder.newFile('src/kotlin/do.not.include')

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHasContent()
    }

    // TODO: Configuring after being explicit (calling either withGroovyDsl() or withKotlinDsl()) overrides the default configuration
}
