package org.gradle.docs.samples

import org.gradle.docs.TestFile
import org.gradle.testkit.runner.BuildResult

abstract class AbstractBothDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected List<TestFile> getDslZipFiles() {
        return [groovyDslZipFile, kotlinDslZipFile]
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertBothDslSampleTasksExecutedAndNotSkipped(result)
    }

    @Override
    protected void assertReadmeHasContent() {
        def groovyReadmeFile = file("build/working/samples/install/demo/groovy/README")
        def kotlinReadmeFile = file("build/working/samples/install/demo/kotlin/README")
        assert groovyReadmeFile.text == """= Demo Sample

[.download]
- link:zips/sample_demo-groovy-dsl.zip[icon:download[] Groovy DSL]
- link:zips/sample_demo-kotlin-dsl.zip[icon:download[] Kotlin DSL]


= Demo Sample

Some doc
"""
        assert groovyReadmeFile.text == kotlinReadmeFile.text
    }

    @Override
    protected void assertDslZipsHaveContent() {
        kotlinDslZipFile.asZip().assertHasDescendants(
                "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar",
                "README",
                "build.gradle.kts", "settings.gradle.kts")
        groovyDslZipFile.asZip().assertHasDescendants(
                "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar",
                "README",
                "build.gradle", "settings.gradle")
    }

    @Override
    protected void assertDslZipFilesExists() {
        groovyDslZipFile.assertExists()
        kotlinDslZipFile.assertExists()
    }
}