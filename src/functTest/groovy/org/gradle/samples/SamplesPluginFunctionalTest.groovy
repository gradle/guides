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
- link:{zip-base-file-name}-groovy-dsl.zip[Download Groovy DSL ZIP]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
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
        result.task(":generateSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":asciidocSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":assemble").outcome == TaskOutcome.SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
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

    def "can assemble sample using a lifecycle task"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        groovyDslZipFile.exists()
        kotlinDslZipFile.exists()
        new File(projectDir, "build/gradle-samples/demo/index.html").exists()
        !new File(projectDir, "build/gradle-samples/index.html").exists()
    }

    def "demonstrate publishing samples to directory"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
def publishTask = tasks.register("publishSamples", Copy) {
    // TODO: The `gradle-samples` directory is an implementation detail
    from("build/gradle-samples")
    into("build/docs/samples")
}

tasks.assemble.dependsOn publishTask
"""

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":asciidocSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":assemble").outcome == TaskOutcome.SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        groovyDslZipFile.exists()
        kotlinDslZipFile.exists()
        getGroovyDslZipFile(buildDirectoryRelativePath: "docs/samples").exists()
        getKotlinDslZipFile(buildDirectoryRelativePath: "docs/samples").exists()
        new File(projectDir, "build/docs/samples/demo/index.html").exists()
        new File(projectDir, "build/docs/samples/index.html").exists()
    }

    def "does not affect Sample compression tasks when configuring Zip type tasks"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
tasks.withType(Zip).configureEach {
    archiveVersion = "4.2"
}
"""

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":asciidocSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":assemble").outcome == TaskOutcome.SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        groovyDslZipFile.exists()
        kotlinDslZipFile.exists()
        !getGroovyDslZipFile(version: '4.2').exists()
        !getKotlinDslZipFile(version: '4.2').exists()
    }

    def "includes project version inside sample zip name"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
version = '5.6.2'
"""

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":asciidocSampleIndex").outcome == TaskOutcome.SUCCESS
        result.task(":assemble").outcome == TaskOutcome.SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        getGroovyDslZipFile(version: '5.6.2').exists()
        getKotlinDslZipFile(version: '5.6.2').exists()
        !groovyDslZipFile.exists()
        !kotlinDslZipFile.exists()
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        sampleIndexFile.exists()
        sampleIndexFile.text.contains('<a href="demo-5.6.2-groovy-dsl.zip">')
        sampleIndexFile.text.contains('<a href="demo-5.6.2-kotlin-dsl.zip">')
        !sampleIndexFile.text.contains('<a href="demo-groovy-dsl.zip">')
        !sampleIndexFile.text.contains('<a href="demo-kotlin-dsl.zip">')
    }

    private static void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        result.task(":generateWrapperForDemoSample").outcome == TaskOutcome.SUCCESS
        result.task(":syncDemoGroovyDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":syncDemoKotlinDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":compressDemoGroovyDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":compressDemoKotlinDslSample").outcome == TaskOutcome.SUCCESS
        result.task(":assembleDemoSample").outcome == TaskOutcome.SUCCESS
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
