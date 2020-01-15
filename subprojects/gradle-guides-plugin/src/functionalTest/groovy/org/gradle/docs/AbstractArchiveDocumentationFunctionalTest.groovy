package org.gradle.docs


import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.containsString

abstract class AbstractArchiveDocumentationFunctionalTest extends AbstractDocumentationFunctionalTest {
    @Unroll
    def "can assemble documentation archives using a lifecycle task"() {
        makeSingleProject()
        writeDocumentationUnderTest()

        when:
        def result = build(assembleDocumentationUnderTestTaskName)

        then:
        result.assertTasksExecutedAndNotSkipped(allTaskToAssembleDocumentationUnderTest)
        assertDslZipFilesExists()
    }

    def "defaults to Gradle version based on the running distribution"() {
        makeSingleProject()
        writeDocumentationUnderTest()

        when:
        usingGradleVersion("6.0")
        build(assembleDocumentationUnderTestTaskName)

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '6.0')
        }

        when:
        usingGradleVersion('6.0.1')
        build(assembleDocumentationUnderTestTaskName)

        then:
        dslZipFiles.each {
            assertGradleWrapperVersion(it, '6.0.1')
        }
    }

    protected abstract void makeSingleProject()
    protected abstract void writeDocumentationUnderTest()
    protected abstract String getAssembleDocumentationUnderTestTaskName()
    protected abstract List<String> getAllTaskToAssembleDocumentationUnderTest()
    protected abstract void assertDslZipFilesExists()
    protected abstract List<TestFile> getDslZipFiles()

    protected static void assertGradleWrapperVersion(TestFile file, String expectedGradleVersion) {
        file.asZip().assertDescendantHasContent('gradle/wrapper/gradle-wrapper.properties', containsString("-${expectedGradleVersion}-"))
    }
}
