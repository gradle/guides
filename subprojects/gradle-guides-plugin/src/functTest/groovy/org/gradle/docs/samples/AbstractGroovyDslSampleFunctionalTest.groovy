package org.gradle.docs.samples

import org.gradle.docs.guides.TestFile

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
                "README",
                "build.gradle", "settings.gradle")
    }

    @Override
    protected void assertDslZipFilesExists() {
        groovyDslZipFile.assertExists()
        kotlinDslZipFile.assertDoesNotExist()
    }

    @Override
    protected void assertReadmeHasContent() {
        def groovyReadmeFile = file("build/working/samples/install/demo/groovy/README")
        def kotlinReadmeFile = file("build/working/samples/install/demo/kotlin/README")
        assert groovyReadmeFile.text == """= Demo Sample

[.download]
- link:zips/sample_demo-groovy-dsl.zip[icon:download[] Groovy DSL]


= Demo Sample

Some doc
"""
        kotlinReadmeFile.assertDoesNotExist()
    }
}