package org.gradle.samples

import org.gradle.guides.TestFile

abstract class AbstractGroovyDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected List<TestFile> getDslZipFiles() {
        return [groovyDslZipFile]
    }

    @Override
    protected void assertDslZipsHaveContent() {
        kotlinDslZipFile.assertDoesNotExist()
        groovyDslZipFile.asZip().assertHasDescendants(
                "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar",
                "sample_demo.adoc",
                "build.gradle", "settings.gradle")
    }

    @Override
    protected void assertDslZipFilesExists() {
        groovyDslZipFile.assertExists()
        kotlinDslZipFile.assertDoesNotExist()
    }
}