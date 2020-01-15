package org.gradle.docs.samples

import org.gradle.docs.Dsl
import org.gradle.docs.TestFile

trait SamplesTrait {
    static String createSample(String name) {
        return """
            documentation.samples.publishedSamples.create('$name')
        """
    }

    static String createSampleWithBothDsl(String name) {
        return """
            documentation.samples.publishedSamples.create('$name') {
                dsls = [${Dsl.canonicalName}.KOTLIN, ${Dsl.canonicalName}.GROOVY]
            }
        """
    }

    static String configureSampleKotlinDsl(String name) {
        return """
            ${sampleDsl(name)}.dsls.add(${Dsl.canonicalName}.KOTLIN)
        """
    }

    static String configureSampleGroovyDsl(String name) {
        return """
            ${sampleDsl(name)}.dsls.add(${Dsl.canonicalName}.GROOVY)
        """
    }

    static String sampleDsl(String name) {
        return "documentation.samples.publishedSamples.${name}"
    }

    static void writeReadmeTo(TestFile directory) {
        directory.file('README.adoc') << '''
            |= Demo Sample
            |
            |Some doc
            |'''.stripMargin()
    }

    static void writeGroovyDslSampleTo(TestFile directory) {
        directory.file('build.gradle') << '''
            |// tag::println[]
            |println "Hello, world!"
            |// end:println[]
            |'''.stripMargin()
        directory.file('settings.gradle') << '''
            |// tag::root-project-name[]
            |rootProject.name = 'demo'
            |// end:root-project-name[]
            |'''.stripMargin()
    }

    static void writeKotlinDslSampleTo(TestFile directory) {
        directory.file('build.gradle.kts') << '''
            |// tag::println[]
            |println("Hello, world!")
            |// end:println[]
            |'''.stripMargin()
        directory.file('settings.gradle.kts') << '''
            |// tag::root-project-name[]
            |rootProject.name = "demo"
            |// end:root-project-name[]
            |'''.stripMargin()
    }

    static TestFile groovyDslZipFile(TestFile buildDirectory, String name) {
        return buildDirectory.file("working/samples/zips/sample_${name}-groovy-dsl.zip")
    }

    static TestFile kotlinDslZipFile(TestFile buildDirectory, String name) {
        return buildDirectory.file("working/samples/zips/sample_${name}-kotlin-dsl.zip")
    }

    static List<String> allTaskToAssemble(String name) {
        return [":${assembleTaskName(name)}", ':generateWrapperForSamples', ":generate${name.capitalize()}Page", ":zipSample${name.capitalize()}Groovy", ":zipSample${name.capitalize()}Kotlin"]
    }

    static String assembleTaskName(String name) {
        return "assemble${name.capitalize()}Sample"
    }
}