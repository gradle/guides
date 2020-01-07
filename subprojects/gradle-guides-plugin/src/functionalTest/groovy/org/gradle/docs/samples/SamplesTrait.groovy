package org.gradle.docs.samples

import org.gradle.docs.TestFile

trait SamplesTrait {
    static String createSample(String name, Dsl... dsls = [Dsl.GROOVY, Dsl.KOTLIN]) {
        def result = new StringBuffer()
        if (dsls.length > 0) {
            result.append("import ${Dsl.canonicalName}\n")
        }
        result.append("documentation.samples.publishedSamples.create('${name}')")
        if (dsls.length > 0) {
            result.append(""" {
                |    dsls = [${dsls.collect { "Dsl.${it.name()}" }.join(', ')}]
                |}""".stripMargin())
        }
        result.append('\n')
        return result
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
}