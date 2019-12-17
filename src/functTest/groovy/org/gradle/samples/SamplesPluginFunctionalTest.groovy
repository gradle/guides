package org.gradle.samples


import spock.lang.Unroll

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.hamcrest.CoreMatchers.equalTo

class SamplesPluginFunctionalTest extends AbstractSampleFunctionalSpec {
    def "demonstrate publishing samples to directory"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
tasks.register("publishSamples", Sync) {
    from(samples.distribution.renderedDocumentation)
    into("build/published/samples/")
}
"""
        when:
        build('publishSamples')

        then:
        file("build/published/samples/index.html").assertExists()
        file("build/published/samples/sample_demo.html").assertExists()
        file("build/published/samples/zips/sample_demo-groovy-dsl.zip").assertExists()
        file("build/published/samples/zips/sample_demo-groovy-dsl.zip").assertExists()
    }

    def "can generate content for the sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
abstract class GenerateTask extends DefaultTask {
    @OutputFile
    abstract RegularFileProperty getOutputFile()
    
    @TaskAction
    void generate() {
        outputFile.get().asFile.text = "This is generated content"
    }
}
def generatorTask = tasks.register("generate", GenerateTask) {
    outputFile = new File(temporaryDir, "generated.txt")
}
${sampleUnderTestDsl}.common {
    from(generatorTask)
}
"""

        when:
        build("assembleDemoSample")

        then:
        result.task(":generate").outcome == SUCCESS
        file("build/sample-zips/sample_demo-groovy-dsl.zip").asZip().assertContainsDescendants("generated.txt")
        file("build/sample-zips/sample_demo-groovy-dsl.zip").asZip().assertContainsDescendants("generated.txt")
    }

    def "fails when settings.gradle.kts is missing from Kotlin DSL sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        file("src/samples/demo/kotlin/settings.gradle.kts").delete()

        when:
        buildAndFail("validateSampleDemoKotlin")

        then:
        result.task(":validateSampleDemoKotlin").outcome == FAILED
        result.output.contains("Sample 'demoKotlin' for Kotlin DSL is invalid due to missing 'settings.gradle.kts' file.")
    }

    def "fails when settings.gradle is missing from Groovy DSL sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        file("src/samples/demo/groovy/settings.gradle").delete()

        when:
        buildAndFail("validateSampleDemoGroovy")

        then:
        result.task(":validateSampleDemoGroovy").outcome == FAILED
        result.output.contains("Sample 'demoGroovy' for Groovy DSL is invalid due to missing 'settings.gradle' file.")
    }

    def "fails when documentation is missing from sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        file("src/samples/demo/README.adoc").delete()
        when:
        // If the readme doesn't exist for the sample, we fail to generate the sample page
        buildAndFail("assembleDemoSample")
        then:
        result.task(":generateDemoPage").outcome == FAILED

        when:
        // Instead of generating the readme, point to a non-existent file
        buildFile << """
${sampleUnderTestDsl}.samplePageFile = file("README.adoc")
"""
        and:
        buildAndFail("validateSampleDemoGroovy")
        then:
        result.task(":validateSampleDemoGroovy").outcome == FAILED

        when:
        buildAndFail("validateSampleDemoKotlin")
        then:
        result.task(":validateSampleDemoKotlin").outcome == FAILED
    }

    def "fails when zip looks invalid"() {
        makeSingleProject()
        writeSampleUnderTest()
        // To simulate an invalid zip, replace the generated zip with an empty one
        file("a.file").text = "This is not a sample."
        buildFile << """
task generateZip(type: Zip) {
    archiveBaseName = "not-a-sample"
    from("a.file")
}

samples.binaries.configureEach {
    zipFile = tasks.generateZip.archiveFile
}
"""
        when:
        buildAndFail("validateSampleDemoGroovy")

        then:
        result.task(":validateSampleDemoGroovy").outcome == FAILED
        result.output.contains("Sample 'demoGroovy' for Groovy DSL is invalid due to missing 'README' file.")

        when:
        buildAndFail("validateSampleDemoKotlin")

        then:
        result.task(":validateSampleDemoKotlin").outcome == FAILED
        result.output.contains("Sample 'demoKotlin' for Kotlin DSL is invalid due to missing 'README' file.")
    }

    def "fails if the sample uses no dsls"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
${sampleUnderTestDsl}.dsls = [] 
        """

        when:
        buildAndFail('assembleDemoSample')

        then:
        result.output.contains("Samples must have at least one DSL, sample 'demo' has none.")
    }

    def "detects DSL based on content available"() {
        makeSingleProject()
        def sampleDirectory = file("src/samples/demo")
        writeReadmeTo(sampleDirectory)
        writeGroovyDslSample(sampleDirectory)

        when:
        // Only expect Groovy DSL content
        buildFile << """
import ${Dsl.canonicalName}
${sampleUnderTestDsl}.dsls = [ Dsl.GROOVY ]
"""
        and:
        build('check')
        then:
        noExceptionThrown()
    }

    def "sample index contains description"() {
        makeSingleProject()
        writeSampleUnderTest()
        def indexFile = file("build/working/samples/docs/index.adoc")

        when:
        build('assemble')
        then:
        indexFile.text.contains('- <<sample_demo#,Demo>>')

        when:
        buildFile << """
            ${sampleUnderTestDsl}.description = "Some description"     
        """
        and:
        build('assemble')
        then:
        indexFile.text.contains('- <<sample_demo#,Demo>>: Some description')
    }

    @Unroll
    def "uses '#displayName' instead of '#name' when generating sample index"() {
        writeSampleUnderTest("src/samples/${name}")
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                publishedSamples {
                    ${name} {
                        sampleDirectory = samplesRoot.dir("${name}")
                    }
                }
            }
        """

        when:
        build('generateSampleIndex')

        then:
        def indexFile = file("build/tmp/generateSampleIndex/index.adoc")
        indexFile.text.contains("${displayName}")

        where:
        name      | displayName
        'foobar'  | 'Foobar'
        'fooBar'  | 'Foo Bar'
        'fooABar' | 'Foo A Bar'
    }

    def "can configure sample display name on the generated sample index"() {
        writeSampleUnderTest('src/samples/demo')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                publishedSamples {                
                    demo {
                        displayName = "Demo XUnit"
                    }
                }
            }
        """

        when:
        build('generateSampleIndex')

        then:
        def indexFile = file("build/tmp/generateSampleIndex/index.adoc")
        indexFile.text.contains("Demo XUnit")
    }

    def "can use template for source of common content"() {
        makeSingleProject()
        writeSampleUnderTest()
        file("src/samples/templates/template-dir/a.txt") << "aaaa"
        file("src/samples/templates/template-dir/subdir/b.txt") << "bbbb"
        buildFile << """
samples {
    templates {
        templateDir
    }
    publishedSamples {
        demo {
            common {
                from(templates.templateDir)
            }
        }
    }
}
"""
        when:
        build("assembleDemoSample")

        then:
        def demoGroovyZip = file("build/sample-zips/sample_demo-groovy-dsl.zip").asZip()
        demoGroovyZip.assertDescendantHasContent("a.txt", equalTo("aaaa"))
        demoGroovyZip.assertDescendantHasContent("subdir/b.txt", equalTo("bbbb"))

        def demoKotlinZip = file("build/sample-zips/sample_demo-groovy-dsl.zip").asZip()
        demoKotlinZip.assertDescendantHasContent("a.txt", equalTo("aaaa"))
        demoKotlinZip.assertDescendantHasContent("subdir/b.txt", equalTo("bbbb"))
    }

    def "can execute the sample from the zip"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build('assembleDemoSample')

        then:
        assertCanRunHelpTask(groovyDslZipFile)
        assertCanRunHelpTask(kotlinDslZipFile)
    }

    def "sorts samples by category and display name"() {
        makeSingleProject()
        buildFile << """
            samples.publishedSamples.create("zzz") {
                category = "Special"
            }
            samples.publishedSamples.create("mmm")
            samples.publishedSamples.create("aaa")
            samples.publishedSamples.all { dsls = [ ${Dsl.canonicalName}.GROOVY ] }
        """

        when:
        build('generateSampleIndex')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS

        and:
        def indexFile = file("build/tmp/generateSampleIndex/index.adoc")
        indexFile.text == """= Sample Index

== Special

- <<sample_zzz#,Zzz>>

== Uncategorized

- <<sample_aaa#,Aaa>>
- <<sample_demo#,Demo>>
- <<sample_mmm#,Mmm>>

"""
    }

    private void assertCanRunHelpTask(File zipFile) {
        def workingDirectory = new File(temporaryFolder.root, zipFile.name)
        "unzip ${zipFile.getCanonicalPath()} -d ${workingDirectory.getCanonicalPath()}".execute().waitFor()

        assert new File(workingDirectory, 'gradlew').canExecute()

        def process = "${workingDirectory}/gradlew help".execute(null, workingDirectory)
        def stdoutThread = Thread.start { process.in.eachLine { println(it) } }
        def stderrThread = Thread.start { process.err.eachLine { println(it) } }
        process.waitFor(30, TimeUnit.SECONDS)
        assert process.exitValue() == 0
        stdoutThread.join(5000)
        stderrThread.join(5000)
    }
}
