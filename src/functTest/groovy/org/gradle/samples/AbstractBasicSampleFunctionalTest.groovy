package org.gradle.samples

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
        assertZipsHasContent()

        assertSampleIndexContainsLinkToSampleArchives()
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        sampleIndexFile.text.contains('<h1>Demo Sample</h1>')
        sampleIndexFile.text.contains('Some doc')

        def indexFile = new File(projectDir, "build/gradle-samples/index.html")
        indexFile.exists()
        indexFile.text.contains('<a href="demo/">')
    }

    protected abstract void makeSingleProject()

    protected abstract void writeSampleUnderTest()

    protected abstract void assertSampleTasksExecutedAndNotSkipped(BuildResult result)

    protected abstract void assertSampleIndexContainsLinkToSampleArchives()

    protected abstract void assertZipsHasContent()
}
