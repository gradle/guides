package org.gradle.samples

import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import java.nio.file.Files
import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesPluginFunctionalTest extends AbstractSampleFunctionalSpec {
    def "demonstrate publishing samples to directory"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
def publishTask = tasks.register("publishSamples", Copy) {
    // TODO: The `gradle-samples` directory is an implementation detail
    from("build/gradle-samples")
    into("build/docs/samples")
}

tasks.assemble.dependsOn publishTask
"""

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        groovyDslZipFile.exists()
        kotlinDslZipFile.exists()
        getGroovyDslZipFile(buildDirectoryRelativePath: "docs/samples").exists()
        getKotlinDslZipFile(buildDirectoryRelativePath: "docs/samples").exists()
        new File(projectDir, "build/docs/samples/demo/index.html").exists()
        new File(projectDir, "build/docs/samples/index.html").exists()
    }

    def "can generate content for the sample"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << '''
samples.configureEach { sample ->
    def generatorTask = tasks.register("generateContentFor${sample.name.capitalize()}Sample") {
        outputs.dir(layout.buildDirectory.dir("sample-contents/${sample.name}"))
        doLast {
            layout.buildDirectory.dir("sample-contents/${sample.name}/gradle.properties").get().asFile.text = "foo.bar = foobar\\n"
        }
    }
    sample.archiveContent.from(files(generatorTask))
}
'''

        when:
        def result = build("assembleDemoSample")

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        result.task(":generateContentForDemoSample").outcome == SUCCESS
        assertZipHasContent(groovyDslZipFile, "gradlew", "gradlew.bat", "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle", "settings.gradle")
        assertZipHasContent(kotlinDslZipFile, "gradlew", "gradlew.bat", "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar", "README.adoc", "build.gradle.kts", "settings.gradle.kts")
    }

    def "can have two sample with different naming convention"() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                "foo-bar"
                "fooBar"
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
        writeGroovyDslSample("src/samples/demo")
        Files.move(new File(temporaryFolder.root, "src/samples/demo/groovy").toPath(), new File(temporaryFolder.root, "src/samples/demo/kotlin").toPath())

        when:
        def result = buildAndFail("assemble")

        then:
        result.output.contains("Execution failed for task ':installDemoKotlinDslSample'.")
        result.output.contains("Sample 'demo' for Kotlin DSL is invalid due to missing 'settings.gradle.kts' file.")
    }

    def "fails when settings.gradle is missing from Groovy DSL sample"() {
        makeSingleProject()
        writeKotlinDslSample("src/samples/demo")
        Files.move(new File(temporaryFolder.root, "src/samples/demo/kotlin").toPath(), new File(temporaryFolder.root, "src/samples/demo/groovy").toPath())

        when:
        def result = buildAndFail("assemble")

        then:
        result.output.contains("Execution failed for task ':installDemoGroovyDslSample'.")
        result.output.contains("Sample 'demo' for Groovy DSL is invalid due to missing 'settings.gradle' file.")
    }

    def "fails when README.adoc is missing from Groovy DSL sample"() {
        makeSingleProject()
        writeGroovyDslSample("src/samples/demo")

        when:
        def result = buildAndFail("assemble")

        then:
        result.output.contains("Execution failed for task ':installDemoGroovyDslSample'.")
        result.output.contains("Sample 'demo' is invalid due to missing 'README.adoc' file.")
    }

    def "fails when README.adoc is missing from Kotlin DSL sample"() {
        makeSingleProject()
        writeKotlinDslSample("src/samples/demo")

        when:
        def result = buildAndFail("assemble")

        then:
        result.output.contains("Execution failed for task ':installDemoKotlinDslSample'.")
        result.output.contains("Sample 'demo' is invalid due to missing 'README.adoc' file.")
    }

    def "can call sample dsl configuration multiple time"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            ${sampleUnderTestDsl} {
                withGroovyDsl()
                withGroovyDsl()
                withKotlinDsl()
                withKotlinDsl()
            }

            tasks.register('verify') {
                doLast {
                    // This uses internal APIs for asserting the correctness of the test
                    assert ${sampleUnderTestDsl}.dslSampleArchives.size() == 2
                }
            }
        """

        when:
        build('verify')

        then:
        noExceptionThrown()
    }

    def "can call sample dsl configuration multiple time to configure components"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            ${sampleUnderTestDsl} {
                withGroovyDsl { println 'configuring Groovy DSL first time' }
                withGroovyDsl { println 'configuring Groovy DSL second time' }
                withKotlinDsl { println 'configuring Kotlin DSL first time' }
                withKotlinDsl { println 'configuring Kotlin DSL second time' }
            }
        """

        when:
        def result = build('help')

        then:
        noExceptionThrown()
        result.output.contains('configuring Groovy DSL first time')
        result.output.contains('configuring Groovy DSL second time')
        result.output.contains('configuring Kotlin DSL first time')
        result.output.contains('configuring Kotlin DSL second time')
    }

    def "sample index contains description"() {
        makeSingleProject()
        writeSampleUnderTest()
        buildFile << """
            ${sampleUnderTestDsl}.description = "Some description"
        """

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)
        def indexFile = new File(projectDir, "build/gradle-samples/index.html")
        indexFile.text.contains('Some description')
    }

    @Unroll
    def "uses '#displayName' instead of '#name' when generating sample index"() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples.create("${name}")
        """
        writeSampleUnderTestToDirectory("src/samples/${name}")

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result, name)

        and:
        def indexFile = new File(projectDir, "build/gradle-samples/index.html")
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
                demoXUnit {
                    displayName = "Demo XUnit"
                }
            }
        """

        when:
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        result.task(":assemble").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result, 'demoXUnit')

        and:
        def indexFile = new File(projectDir, "build/gradle-samples/index.html")
        indexFile.text.contains("Demo XUnit")
    }

    def "can use sample display name inside Asciidoctor file"() {
        makeSingleProject()
        writeSampleUnderTest()
        sampleReadMeFile << """
== {sample-displayName}
"""
        buildFile << """
            samples.demo.displayName = "Some Display Name"
        """

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)

        and:
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        sampleIndexFile.text.contains('<h2 id="some_display_name">Some Display Name</h2>')
        !sampleIndexFile.text.contains('{sample-displayName}')
    }

    def "can use sample description inside Asciidoctor file"() {
        makeSingleProject()
        writeSampleUnderTest()
        sampleReadMeFile << """
{sample-description}
"""
        buildFile << """
            samples.demo.description = "Some description"
        """

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)

        and:
        def sampleIndexFile = new File(projectDir, "build/gradle-samples/demo/index.html")
        sampleIndexFile.text.contains('Some description')
        !sampleIndexFile.text.contains('{sample-description}')
    }

    def "can execute the sample from the zip"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        def result = build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)

        and:
        assertCanRunHelpTask(groovyDslZipFile)
        assertCanRunHelpTask(kotlinDslZipFile)
    }

    def "honors the sample declaration order in the generated sample index"() {
        writeSampleUnderTestToDirectory('src/samples/foo')
        writeSampleUnderTestToDirectory('src/samples/bar')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                foo
                bar
            }
        """

        when:
        def result = build('asciidocSampleIndex')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS

        and:
        def indexFile = new File(projectDir, "build/gradle-samples/index.html").readLines()
        indexFile.findIndexOf { it.contains('Foo') } < indexFile.findIndexOf { it.contains('Bar') }
    }

    @Unroll
    def "ensure the zip file name is capitalized (#sampleName => #expectedZipName)"() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples.create('${sampleName}')
        """
        writeSampleUnderTestToDirectory("src/samples/${sampleName}")

        when:
        assert !getGroovyDslZipFile([name: sampleName]).exists()
        assert !getKotlinDslZipFile([name: sampleName]).exists()
        def result = build("assemble${sampleName.capitalize()}Sample")

        then:
        assertSampleTasksExecutedAndNotSkipped(result, sampleName)
        getGroovyDslZipFile([name: sampleName]).exists()
        getKotlinDslZipFile([name: sampleName]).exists()

        where:
        sampleName | expectedZipName
        'foo'      | 'Foo'
        'fooBar'   | 'FooBar'
        'foo-bar'  | 'Foo-bar'
        'foo-Bar'  | 'Foo-Bar'
        'foo_bar'  | 'Foo_bar'
        'foo_Bar'  | 'Foo_Bar'
    }

    def "defaults permalink to sample name (#sampleName)"() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            tasks.register('verify') {
                doLast {
                    assert samples.getByName('${sampleName}').permalink.get() == '${sampleName}'
                }
            }

            samples.create('$sampleName')
        """

        expect:
        build('verify')

        where:
        sampleName << ['foo', 'fooBar', 'foo_bar', 'foo-bar']
    }

    def "can customize the permalink of a sample"() {
        writeSampleUnderTest()
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                demo {
                    permalink = 'foo_bar'
                }
            }
        """

        when:
        assert !new File(projectDir, 'build/gradle-samples/foo_bar').exists()
        assert !new File(projectDir, 'build/gradle-samples/demo').exists()
        def result = build('assemble')

        then:
        result.task(":generateSampleIndex").outcome == SUCCESS
        result.task(":asciidocSampleIndex").outcome == SUCCESS
        assertSampleTasksExecutedAndNotSkipped(result)

        and:
        def indexFile = new File(projectDir, "build/gradle-samples/index.html")
        indexFile.text.contains("""<a href="foo_bar/">""")
        !indexFile.text.contains("""<a href="demo/">""")

        and:
        !new File(projectDir, "build/gradle-samples/demo/index.html").exists()
        !getKotlinDslZipFile().exists()
        !getGroovyDslZipFile().exists()

        and:
        new File(projectDir, "build/gradle-samples/foo_bar/index.html").exists()
        getKotlinDslZipFile([permalink: 'foo_bar']).exists()
        getGroovyDslZipFile([permalink: 'foo_bar']).exists()
    }

    def "does not fail or warn when install task with duplicated permalink are absent from task graph"() {
        writeSampleUnderTestToDirectory('src/samples/foo')
        writeSampleUnderTestToDirectory('src/samples/bar')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                foo {
                    permalink = 'a_sample'
                }
                bar {
                    permalink = 'a_sample'
                }
            }
        """

        when:
        def result = build('help')

        then:
        result.task(':help').outcome == SUCCESS
        !result.output.contains("""Permalinks collision detected among samples:
 * The following samples are sharing permalink 'a_sample':
   - Sample 'bar'
   - Sample 'foo'""")
    }

    def "warn when one of the install task with duplicated permalink is present in task graph"() {
        writeSampleUnderTestToDirectory('src/samples/foo')
        writeSampleUnderTestToDirectory('src/samples/bar')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                foo {
                    permalink = 'a_sample'
                }
                bar {
                    permalink = 'a_sample'
                }
            }
        """

        when:
        def result = build('assembleFooSample', '-w')

        then:
        assertSampleTasksExecutedAndNotSkipped(result, 'foo')

        and:
        result.output.contains("""Permalinks collision detected among samples:
 * The following samples are sharing permalink 'a_sample':
   - Sample 'bar'
   - Sample 'foo'""")
    }

    def "fails when multiple install task with duplicated permalink are present in task graph"() {
        writeSampleUnderTestToDirectory('src/samples/foo')
        writeSampleUnderTestToDirectory('src/samples/bar')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                foo {
                    permalink = 'a_sample'
                }
                bar {
                    permalink = 'a_sample'
                }
            }
        """

        when:
        def result = buildAndFail('installBarSample', 'installFooSample')

        then:
        result.output.contains("""Permalinks collision detected among samples:
 * The following samples are sharing permalink 'a_sample':
   - Sample 'bar'
   - Sample 'foo'""")
    }

    def "fails when sample index contains duplicated permalink"() {
        writeSampleUnderTestToDirectory('src/samples/foo')
        writeSampleUnderTestToDirectory('src/samples/bar')
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                foo {
                    permalink = 'a_sample'
                }
                bar {
                    permalink = 'a_sample'
                }
            }
        """

        when:
        def result = buildAndFail('asciidocSampleIndex')

        then:
        result.output.contains("""Permalinks collision detected among samples:
 * The following samples are sharing permalink 'a_sample':
   - Sample 'bar'
   - Sample 'foo'""")
    }

    // TODO: Allow preprocess build script files before zipping (remove tags, see NOTE1) or including them in rendered output (remove tags and license)
    //   NOTE1: We can remove the license from all the files and add a LICENSE file at the root of the sample

    protected void makeSingleProject() {
        buildFile << """
            plugins {
                id 'org.gradle.samples'
            }

            samples {
                demo
            }
        """
    }

    protected void writeSampleUnderTest() {
        writeSampleUnderTestToDirectory('src/samples/demo')
    }

    protected void writeSampleUnderTestToDirectory(String directory) {
        writeSampleContentToDirectory(directory) << """
ifndef::env-github[]
- link:{zip-base-file-name}-groovy-dsl.zip[Download Groovy DSL ZIP]
- link:{zip-base-file-name}-kotlin-dsl.zip[Download Kotlin DSL ZIP]
endif::[]
"""
        writeGroovyDslSample(directory);
        writeKotlinDslSample(directory)
    }

    protected static void assertSampleTasksExecutedAndNotSkipped(BuildResult result, String name = 'demo') {
        assertBothDslSampleTasksExecutedAndNotSkipped(result, name)
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
