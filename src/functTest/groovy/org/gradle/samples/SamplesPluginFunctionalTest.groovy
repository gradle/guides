package org.gradle.samples

import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.testkit.runner.TaskOutcome

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesPluginFunctionalTest extends AbstractSampleFunctionalTest {
    def "can build samples"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
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
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
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
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
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
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
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

    def "can add more attributes to AsciidoctorTask types before and after samples are added"() {
        makeSingleProject()
        buildFile << """
import ${AsciidoctorTask.canonicalName}

tasks.withType(AsciidoctorTask).configureEach {
    attributes 'prop1': 'value1'
}

tasks.register('verify') {
    doLast {
        def allAsciidoctorTasks = tasks.withType(AsciidoctorTask)
        assert allAsciidoctorTasks.collect { it.attributes.prop1 } == ['value1'] * allAsciidoctorTasks.size()
    }
}

samples.create('anotherDemo')
"""

        when:
        build('verify')

        then:
        noExceptionThrown()
    }

    def "defaults Gradle version based on the running distribution"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        usingGradleVersion("5.4.1")
        build("assembleDemoSample")

        then:
        assertGradleWrapperVersion(groovyDslZipFile, '5.4.1')
        assertGradleWrapperVersion(kotlinDslZipFile, '5.4.1')

        when:
        usingGradleVersion('5.6.2')
        build("assembleDemoSample")

        then:
        assertGradleWrapperVersion(groovyDslZipFile, '5.6.2')
        assertGradleWrapperVersion(kotlinDslZipFile, '5.6.2')
    }

    def "can change sample Gradle version"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        usingGradleVersion("5.4.1")
        buildFile << """
${sampleUnderTestDsl} {
    gradleVersion = "5.6.2"
}
"""
        build("assembleDemoSample")

        then:
        assertGradleWrapperVersion(groovyDslZipFile, '5.6.2')
        assertGradleWrapperVersion(kotlinDslZipFile, '5.6.2')
    }

    def "can generate content for the sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << '''
samples.configureEach { sample ->
    def generatorTask = tasks.register("generateContentFor${sample.name.capitalize()}Sample") {
        outputs.dir(layout.buildDirectory.dir("sample-contents/${sample.name}"))
        doLast {
            layout.buildDirectory.dir("sample-contents/${sample.name}/gradle.properties").get().asFile.text = "foo.bar = foobar\\n"
        }
    }
    sample.archiveContent.from(files(generatorTask))
}
'''

        when:
        def result = build("assembleDemoSample")

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        result.task(":generateContentForDemoSample").outcome == SUCCESS
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle")
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")
    }

    // TODO: Allow preprocess build script files before zipping (remove tags, see NOTE1) or including them in rendered output (remove tags and license)
    //   NOTE1: We can remove the license from all the files and add a LICENSE file at the root of the sample

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

    private static void assertGradleWrapperVersion(File file, String expectedGradleVersion) {
        assert file.exists()
        def text = new ZipFile(file).withCloseable { zipFile ->
            return zipFile.getInputStream(zipFile.entries().findAll { !it.directory }.find { it.name == 'gradle/wrapper/gradle-wrapper.properties' }).text
        }

        assert text.contains("-${expectedGradleVersion}-")
    }
}
