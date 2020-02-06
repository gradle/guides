package org.gradle.docs.samples

import org.gradle.docs.TestFile

abstract class AbstractKotlinDslSampleFunctionalTest extends AbstractBasicSampleFunctionalTest {
    @Override
    protected List<TestFile> getDslZipFiles() {
        return [kotlinDslZipFile]
    }

    @Override
    protected void assertDslZipsHaveContent() {
        groovyDslZipFile.assertDoesNotExist()
        kotlinDslZipFile.asZip().assertHasDescendants("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README", "build.gradle.kts", "settings.gradle.kts")
    }

    @Override
    protected void assertDslZipFilesExists() {
        groovyDslZipFile.assertDoesNotExist()
        kotlinDslZipFile.assertExists()
    }

    @Override
    protected void assertReadmeHasContent() {
        def groovyReadmeFile = file("build/working/samples/install/demo/groovy/README")
        def kotlinReadmeFile = file("build/working/samples/install/demo/kotlin/README")
        assert kotlinReadmeFile.text == """:samples-dir: ${file('/build/working/samples/install/demo')}
            |:gradle-version: ${gradleVersion}
            |
            |= Demo Sample
            |
            |[.download]
            |- link:zips/sample_demo-kotlin-dsl.zip[icon:download[] Kotlin DSL]
            |
            |
            |= Demo Sample
            |
            |Some doc
            |""".stripMargin()
        groovyReadmeFile.assertDoesNotExist()
    }

    @Override
    protected String configureAsciidoctorIncludeSample() {
        return '''
            |====
            |include::sample[dir="kotlin", files="settings.gradle.kts[]"]
            |====
            |'''.stripMargin()
    }
}
