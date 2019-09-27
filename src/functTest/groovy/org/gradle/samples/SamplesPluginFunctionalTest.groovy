package org.gradle.samples

import org.gradle.guides.AbstractFunctionalTest
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class SamplesPluginFunctionalTest extends AbstractFunctionalTest {
    private void writeSampleUnderTest() {
        temporaryFolder.newFolder("src")
        temporaryFolder.newFile("src/README.adoc") << """
= Demo Sample

Some doc

ifndef::env-github[]
- link:demo-groovy-dsl.zip[Download Groovy DSL ZIP]
- link:demo-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        temporaryFolder.newFolder("src", "groovy")
        temporaryFolder.newFile("src/groovy/build.gradle") << """
            println "Hello, world!"
        """
        temporaryFolder.newFile("src/groovy/settings.gradle") << """
            rootProject.name = 'demo'
        """
        temporaryFolder.newFolder("src", "kotlin")
        temporaryFolder.newFile("src/kotlin/build.gradle.kts") << """
            println("Hello, world!")
        """
        temporaryFolder.newFile("src/kotlin/settings.gradle.kts") << """
            rootProject.name = "demo"
        """
    }

    private void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }
            
            samples {
                create("demo") {
                    sampleDir = file('src')
                }
            }
        """
    }

    protected File getGroovyDslZipFile(Map m = [:]) {
        return getDslZipFile(m + [dsl: 'groovy-dsl'])
    }

    protected File getKotlinDslZipFile(Map m = [:]) {
        return getDslZipFile(m + [dsl: 'kotlin-dsl'])
    }

    private File getDslZipFile(Map m) {
        def versionToken = m.version ? "${m.version}-" : ''
        def buildDirectoryRelativePathToken = m.getOrDefault('buildDirectoryRelativePath', 'gradle-samples')
        def dslToken = m.dsl
        return new File(projectDir, "build/${buildDirectoryRelativePathToken}/demo/demo-${versionToken}${dslToken}.zip")
    }

    def "can build samples"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        def result = build('assemble')

        then:
        assertTasksExecutedAndNotSkipped(result)
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle")
        new File(projectDir, "build/gradle-samples/demo/index.html").exists()
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")

        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        sampleIndexFile.exists()
        sampleIndexFile.text.contains('<a href="demo-groovy-dsl.zip">')
        sampleIndexFile.text.contains('<a href="demo-kotlin-dsl.zip">')
        sampleIndexFile.text.contains('<h1>Demo Sample</h1>')
        sampleIndexFile.text.contains('Some doc')

        def indexFile = new File(projectDir, "build/gradle-samples/index.html")
        indexFile.exists()
        indexFile.text.contains('<a href="demo/">')
    }

    private static void assertTasksExecutedAndNotSkipped(BuildResult result) {
        result.task(":generateSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":asciidocSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":generateWrapperForDemoSample").outcome == TaskOutcome.SUCCESS
        result.task(":syncDemoGroovyDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":syncDemoKotlinDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":compressDemoGroovyDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":compressDemoKotlinDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":assemble").outcome == TaskOutcome.SUCCESS
    }

    private static void assertZipHasContent(File file, String... expectedContent) {
        assert file.exists()
        def content = new ZipFile(file).withCloseable { zipFile ->
            return zipFile.entries().findAll { !it.directory }.collect { ZipEntry zipEntry ->
                return zipEntry.getName()
            }
        } as Set

        assert content.size() == expectedContent.size()
        content.removeAll(Arrays.asList(expectedContent))
        assert content.empty
    }
}
