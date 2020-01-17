package org.gradle.docs.samples


import org.gradle.docs.TestFile
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

abstract class AbstractBasicSampleFunctionalTest extends AbstractSampleFunctionalSpec {
    def "can build samples"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":generateWrapperForSamples").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        and:
        def indexFile = file("build/working/samples/docs/index.adoc")
        indexFile.text.contains('- <<sample_demo#,Demo>>')
        assertReadmeHasContent()
        and:
        assertDslZipsHaveContent()
    }

    def "can assemble sample using a lifecycle task"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipFilesExists()
    }

    def "defaults to Gradle version based on the running distribution"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        usingGradleVersion("6.0")
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '6.0')
        }

        when:
        usingGradleVersion('6.0.1')
        build("assembleDemoSample")

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '6.0.1')
        }
    }

    def "can relocate sample"() {
        makeSingleProject()
        writeSampleUnderTest(file('src'))
        buildFile << """
            ${sampleUnderTestDsl} {
                sampleDirectory = file('src')
            }
        """

        when:
        build("assembleDemoSample")

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }

    def "defaults sample location to `src/docs/samples/<sample-name>`"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${sampleUnderTestDsl}.sampleDirectory.get().asFile.absolutePath == '${file('src/docs/samples/demo').canonicalPath}'
                }
            }
        """

        when:
        build("verify")

        then:
        noExceptionThrown()
    }

    @Unroll
    def "excludes '#directory' when building the domain language archive"() {
        makeSingleProject()
        writeSampleUnderTest()
        sampleDirectoryUnderTest.file("common/${directory}/foo.txt") << "Exclude"
        buildFile << """
            def sample = ${sampleUnderTestDsl}
            sample.common {
                from(sample.sampleDirectory.file("common"))
            }
        """
        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()

        where:
        directory << ['.gradle', 'build']
    }

    def "can include sample"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        sampleDirectoryUnderTest.file('README.adoc') << configureAsciidoctorIncludeSample()

        expect:
        build('assemble')
    }

    protected abstract List<TestFile> getDslZipFiles()

    protected abstract void assertSampleTasksExecutedAndNotSkipped(BuildResult result)

    protected abstract void assertReadmeHasContent()

    protected abstract void assertDslZipsHaveContent()

    protected abstract void assertDslZipFilesExists()

    protected abstract String configureAsciidoctorIncludeSample()
}
