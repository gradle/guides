package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

import static org.hamcrest.CoreMatchers.not

class BasicExplicitGroovyDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
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
        assert sampleIndexFile.text.contains("""<a href="Demo${version}-groovy-dsl.zip">""")
        assert !sampleIndexFile.text.contains("""<a href="Demo${version}-kotlin-dsl.zip">""")
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
        assert !sampleIndexFile.text.contains("""<a href="Demo${version}-groovy-dsl.zip">""")
        assert !sampleIndexFile.text.contains("""<a href="Demo${version}-kotlin-dsl.zip">""")
    }

    @Override
    protected void assertDslZipsHasContent(String... additionalFiles) {
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle", *additionalFiles)
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

    @Override
    protected boolean hasGroovyDsl() {
        return true
    }

    @Override
    protected boolean hasKotlinDsl() {
        return false
    }

    @Override
    protected void assertDslZipFilesDoesNotContainsAsciidoctorTags() {
        assertFileInZipThat(groovyDslZipFile, "build.gradle", not(containsAsciidoctorTags()))
        assertFileInZipThat(groovyDslZipFile, "settings.gradle", not(containsAsciidoctorTags()))
    }

    @Override
    protected String useAsciidoctorSampleExtension() {
        return """
====
include::sample[dir="groovy",files="build.gradle[]"]
====
"""
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

    // TODO: Configuring after being explicit (calling either withGroovyDsl() or withKotlinDsl()) overrides the default configuration
}
