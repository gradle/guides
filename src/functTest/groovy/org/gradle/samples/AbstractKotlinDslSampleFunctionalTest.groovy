package org.gradle.samples

import org.gradle.guides.TestFile
import org.gradle.testkit.runner.BuildResult

abstract class AbstractKotlinDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected List<TestFile> getDslZipFiles() {
        return [kotlinDslZipFile]
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyKotlinDslTasksExecutedAndNotSkipped(result)
    }

    @Override
    protected void assertDslZipsHaveContent() {
        groovyDslZipFile.assertDoesNotExist()
        kotlinDslZipFile.asZip().assertHasDescendants("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")
    }

    @Override
    protected void assertDslZipFilesExists() {
        groovyDslZipFile.assertDoesNotExist()
        kotlinDslZipFile.assertExists()
    }
}