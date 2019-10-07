package org.gradle.samples

import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

abstract class AbstractBasicSampleFunctionalTest extends AbstractSampleFunctionalSpec {
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
        new File(projectDir, "build/gradle-samples/demo/index.html").exists()
        assertDslZipsHasContent()

        assertSampleIndexContainsLinkToSampleArchives()
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
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
        assertDslZipFilesExists()
        new File(projectDir, "build/gradle-samples/demo/index.html").exists()
        !new File(projectDir, "build/gradle-samples/index.html").exists()
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
        assertDslZipFilesExists()
        assertDslZipFilesDoesNotExists(version: '4.2')
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
        assertDslZipFilesExists(version: '5.6.2')
        assertDslZipFilesDoesNotExists()
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        sampleIndexFile.exists()
        assertSampleIndexContainsLinkToSampleArchives('5.6.2')
        assertSampleIndexDoesNotContainsLinkToSampleArchives()
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
            
            samples.create('anotherDemo') {
                sampleDir = file('anotherDemo')
            }
        """
        // TODO: SampleDir should have convention of src/samples/<sample-name>

        when:
        build('verify')

        then:
        noExceptionThrown()
    }

    def "can change sample Gradle version"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        usingGradleVersion("5.5.1")
        buildFile << """
            ${sampleUnderTestDsl} {
                gradleVersion = "5.6.2"
            }
        """
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '5.6.2')
        }
    }

    def "defaults Gradle version based on the running distribution"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        usingGradleVersion("5.5.1")
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '5.5.1')
        }

        when:
        usingGradleVersion('5.6.2')
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '5.6.2')
        }
    }

    def "can relocate sample"() {
        makeSingleProject()
        writeSampleUnderTestToDirectory('src')
        buildFile << """
${sampleUnderTestDsl} {
    sampleDir = file('src')
}
"""

        when:
        def result = build("assembleDemoSample")

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHasContent()
    }

    protected abstract void makeSingleProject()

    protected void writeSampleUnderTest() {
        writeSampleUnderTestToDirectory('src')
    }

    protected abstract void writeSampleUnderTestToDirectory(String directory)

    protected abstract List<File> getDslZipFiles(Map m = [:])

    protected abstract void assertSampleTasksExecutedAndNotSkipped(BuildResult result)

    protected abstract void assertSampleIndexContainsLinkToSampleArchives(String version = null)

    protected abstract void assertSampleIndexDoesNotContainsLinkToSampleArchives(String version = null)

    protected abstract void assertDslZipsHasContent()

    protected abstract void assertDslZipFilesExists(Map m = [:])

    protected abstract void assertDslZipFilesDoesNotExists(Map m = [:])
}
