package org.gradle.samples


import spock.lang.Unroll

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesPluginFunctionalTest extends AbstractSampleFunctionalSpec {
    def "demonstrate publishing samples to directory"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
tasks.register("publishSamples", Sync) {
    from(samples.assembledDocumentation)
    into("build/published/samples/")
}
"""
        when:
        build('publishSamples')

        then:
        file("build/published/samples/index_samples.adoc").assertExists()
        file("build/published/samples/sample_demo.adoc").assertExists()
        file("build/published/samples/zips/demoGroovy.zip").assertExists()
        file("build/published/samples/zips/demoKotlin.zip").assertExists()
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
        file("build/install/samples/demo/groovy/generated.txt").text == "This is generated content"
        file("build/install/samples/demo/kotlin/generated.txt").text == "This is generated content"
        file("build/distributions/demoGroovy.zip").asZip().assertContainsContent("generated.txt")
        file("build/distributions/demoKotlin.zip").asZip().assertContainsContent("generated.txt")
    }

    def "can have two samples with different naming convention"() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                publishedSamples {
                    "foo-bar"
                    "fooBar"
                }
            }
        """
        writeGroovyDslSample("src/samples/foo-bar")
        writeKotlinDslSample("src/samples/foo-bar")
        writeGroovyDslSample("src/samples/fooBar")
        writeKotlinDslSample("src/samples/fooBar")

        when:
        build("help")

        then:
        noExceptionThrown()
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
        result.output.contains("Sample 'demoGroovy' for Groovy DSL is invalid due to missing 'sample_demo.adoc' file in ZIP.")

        when:
        buildAndFail("validateSampleDemoKotlin")

        then:
        result.task(":validateSampleDemoKotlin").outcome == FAILED
        result.output.contains("Sample 'demoKotlin' for Kotlin DSL is invalid due to missing 'sample_demo.adoc' file in ZIP.")
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

    def "fails if the sample does not have content for expected DSLs"() {
        makeSingleProject()
        // By default, we expect to produce samples for both Groovy and Kotlin, this should fail.
        writeGroovyDslSample("src/samples/demo")

        when:
        buildAndFail('check')

        then:
        result.task(":validateSampleDemoKotlin").outcome == FAILED

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
        def indexFile = file("build/samples/docs/index_samples.adoc")

        when:
        build('assemble')
        then:
        indexFile.text.contains('- <<sample_demo.adoc,Demo>>')

        when:
        buildFile << """
            ${sampleUnderTestDsl}.description = "Some description"     
        """
        and:
        build('assemble')
        then:
        indexFile.text.contains('- <<sample_demo.adoc,Demo>>: Some description')
    }

    @Unroll
    def "uses '#displayName' instead of '#name' when generating sample index"() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples.publishedSamples.create("${name}")
        """
        writeSampleUnderTestToDirectory("src/samples/${name}")

        when:
        build('assemble')

        then:
        def indexFile = file("build/samples/docs/index_samples.adoc")
        indexFile.text.contains("${displayName}")

        where:
        name      | displayName
        'foobar'  | 'Foobar'
        'fooBar'  | 'Foo Bar'
        'foo-bar' | 'Foo Bar'
        'foo_bar' | 'Foo Bar'
        'fooABar' | 'Foo A Bar'
    }

    def "can configure sample display name on the generated sample index"() {
        writeSampleUnderTestToDirectory('src/samples/demoXUnit')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                publishedSamples {                
                    demoXUnit {
                        displayName = "Demo XUnit"
                    }
                }
            }
        """

        when:
        build('assemble')

        then:
        def indexFile = file("build/samples/docs/index_samples.adoc")
        indexFile.text.contains("Demo XUnit")
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

    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                publishedSamples {
                    demo
                }
            }
        """
    }

    protected void writeSampleUnderTest() {
        writeSampleUnderTestToDirectory('src/samples/demo')
    }

    protected void writeSampleUnderTestToDirectory(String directory) {
        writeSampleContentToDirectory(file(directory))
        writeGroovyDslSample(directory)
        writeKotlinDslSample(directory)
    }

    private void assertCanRunHelpTask(File zipFile) {
        def workingDirectory = file(zipFile.name)
        def ant = new AntBuilder()
        ant.unzip(src: zipFile, dest: workingDirectory)

        workingDirectory.file('gradlew').executable = true
        def process = "${workingDirectory}/gradlew help".execute(null, workingDirectory)
        def stdoutThread = Thread.start { process.in.eachLine { println(it) } }
        def stderrThread = Thread.start { process.err.eachLine { println(it) } }
        process.waitFor(30, TimeUnit.SECONDS)
        assert process.exitValue() == 0
        stdoutThread.join(5000)
        stderrThread.join(5000)
    }
}
