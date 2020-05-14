package org.gradle.docs.samples

import org.gradle.docs.TestFile
import spock.lang.Ignore
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
                from(documentation.samples.distribution.renderedDocumentation)
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
        sampleDirectoryUnderTest.file('kotlin/settings.gradle.kts').delete()

        when:
        buildAndFail("validateSampleDemoKotlin")

        then:
        result.task(":validateSampleDemoKotlin").outcome == FAILED
        result.output.contains("Sample 'demoKotlin' for Kotlin DSL is invalid due to missing 'settings.gradle.kts' file.")
    }

    def "fails when settings.gradle is missing from Groovy DSL sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        sampleDirectoryUnderTest.file('groovy/settings.gradle').delete()

        when:
        buildAndFail("validateSampleDemoGroovy")

        then:
        result.task(":validateSampleDemoGroovy").outcome == FAILED
        result.output.contains("Sample 'demoGroovy' for Groovy DSL is invalid due to missing 'settings.gradle' file.")
    }

    def "fails when documentation is missing from sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        sampleDirectoryUnderTest.file('README.adoc').delete()
        when:
        // If the readme doesn't exist for the sample, we fail to generate the sample page
        buildAndFail("assembleDemoSample")
        then:
        result.task(":generateDemoPage").outcome == FAILED
    }

    def "can configure readme location"() {
        makeSingleProject()
        buildFile << """
            ${sampleUnderTestDsl} {
                readmeFile = file('src/docs/samples/demo/CUSTOM_README.adoc')
            }
        """

        TestFile directory = file('src/docs/samples/demo')
        writeReadmeTo(directory, 'CUSTOM_README.adoc')
        writeGroovyDslSampleTo(directory.file('groovy'))

        when:
        // If the readme doesn't exist for the sample, we fail to generate the sample page
        ExecutionResult result = build("assembleDemoSample")
        then:
        result.task(":generateDemoPage").outcome == SUCCESS
    }

    // TODO: Generalize test
    @Ignore
    def "fails when zip looks invalid"() {
        makeSingleProject()
        writeSampleUnderTest()
        assert file('src/docs/samples/demo/README.adoc').delete()
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
        result.output.contains("Samples must define at least one DSL, sample 'demo' has none.")
    }

    def "detects DSL based on content available"() {
        makeSingleProject()
        writeReadmeTo(sampleDirectoryUnderTest)
        writeGroovyDslSampleTo(sampleDirectoryUnderTest.file('groovy'))

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
        writeSampleUnderTest(file("src/docs/samples/${name}"))
        buildFile << applyDocumentationPlugin() << createSampleWithBothDsl(name)

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
        writeSampleUnderTest()
        buildFile << applyDocumentationPlugin() << createSampleWithBothDsl('demo') << """
            ${sampleDsl('demo')}.displayName = "Demo XUnit"
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
        file("src/docs/samples/templates/template-one/a.txt") << "aaaa"
        file("src/docs/samples/templates/template-one/subdir/b.txt") << "bbbb"
        file("src/docs/samples/templates/template-two/c.txt") << "cccc"
        file("src/docs/samples/templates/template-two/subdir/d.txt") << "dddd"
        file("src/docs/samples/demo/groovy/subdir/e.txt") << "eeee"
        file("src/docs/samples/demo/kotlin/subdir/e.txt") << "eeee"
        buildFile << """
            documentation {
                samples {
                    templates {
                        templateOne
                        templateTwo
                    }
                    publishedSamples {
                        demo {
                            common {
                                from(templates.templateOne)
                                from(templates.templateTwo)
                            }
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
        demoGroovyZip.assertDescendantHasContent("c.txt", equalTo("cccc"))
        demoGroovyZip.assertDescendantHasContent("subdir/d.txt", equalTo("dddd"))

        def demoKotlinZip = file("build/sample-zips/sample_demo-groovy-dsl.zip").asZip()
        demoKotlinZip.assertDescendantHasContent("a.txt", equalTo("aaaa"))
        demoKotlinZip.assertDescendantHasContent("subdir/b.txt", equalTo("bbbb"))
        demoKotlinZip.assertDescendantHasContent("c.txt", equalTo("cccc"))
        demoKotlinZip.assertDescendantHasContent("subdir/d.txt", equalTo("dddd"))
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
            ${createSample('zzz')} {
                category = "Special"
            }
            ${createSample('mmm')}
            ${createSample('aaa')}
            documentation.samples.publishedSamples.all { dsls = [ ${Dsl.canonicalName}.GROOVY ] }
        """

        when:
        build('generateSampleIndex')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS

        and:
        def indexFile = file("build/tmp/generateSampleIndex/index.adoc")
        indexFile.text == """= Sample Index
            |
            |== Special
            |
            |- <<sample_zzz#,Zzz>>
            |
            |== Uncategorized
            |
            |- <<sample_aaa#,Aaa>>
            |- <<sample_demo#,Demo>>
            |- <<sample_mmm#,Mmm>>
            |
            |""".stripMargin()
    }

    def "filters non-promoted sample from samples index"() {
        makeSingleProject()
        buildFile << """
            ${createSample('aaa')}
            ${createSample('bbb')} {
                promoted = false
            }
            ${createSample('ccc')}
            documentation.samples.publishedSamples.all { dsls = [ ${Dsl.canonicalName}.GROOVY ] }
        """

        when:
        build('generateSampleIndex')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS

        and:
        def indexFile = file("build/tmp/generateSampleIndex/index.adoc")
        indexFile.text == """= Sample Index
            |
            |== Uncategorized
            |
            |- <<sample_aaa#,Aaa>>
            |- <<sample_ccc#,Ccc>>
            |- <<sample_demo#,Demo>>
            |
            |""".stripMargin()
    }

    def "omits validation tasks for non-promoted samples"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            documentation.samples.publishedSamples.demo {
               promoted = false
            }
        """

        when:
        build('checkSamples')

        then:
        result.task(':docsTest').outcome == SUCCESS
        result.task(':checkDemoSampleLinks') == null
        result.task(':validateSampleDemoGroovy') == null
        result.task(':validateSampleDemoKotlin') == null
    }

    def "documentation references removed from source code in zip"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build('assembleDemoSample')

        then:
        assertZipEntryDoesNotContainDocRef(groovyDslZipFile, "build.gradle")
        assertZipEntryDoesNotContainDocRef(kotlinDslZipFile, "build.gradle.kts")
    }

    private void assertCanRunHelpTask(File zipFile) {
        File workingDirectory = extract(zipFile)
        assert new File(workingDirectory, 'gradlew').canExecute()
        assertSuccessfulExecution("${workingDirectory}/gradlew help", workingDirectory)
    }

    private void assertZipEntryDoesNotContainDocRef(File zipFile, String entry) {
        File workingDirectory = extract(zipFile)
        File file = new File(workingDirectory, entry)
        assert !["// tag::", "// end::"].any { file.text.contains(it) }
    }

    private File extract(File zipFile) {
        File workingDirectory = new File(temporaryFolder.root, zipFile.name)
        workingDirectory.mkdirs()
        assertSuccessfulExecution("unzip ${zipFile.getCanonicalPath()} -d ${workingDirectory.getCanonicalPath()}")
        workingDirectory
    }

    private void assertSuccessfulExecution(String commandLine, File workingDirectory = null) {
        def process = commandLine.execute(null, workingDirectory)
        def stdoutThread = Thread.start { process.in.eachByte { print(new String(it)) } }
        def stderrThread = Thread.start { process.err.eachByte { print(new String(it)) } }
        assert process.waitFor(30, TimeUnit.SECONDS)
        assert process.exitValue() == 0
        stdoutThread.join(5000)
        stderrThread.join(5000)
    }
}
