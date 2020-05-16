package org.gradle.docs.samples

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

abstract class AbstractTestWithExemplarSampleFunctionalTest extends AbstractSampleFunctionalSpec implements SamplesTrait {
    def "can test sample using exemplar"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << expectTestsExecuted(getExpectedTestsFor("demo"))

        build("generateSamplesExemplarFunctionalTest")
        when:
        build('docsTest')
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
    }

    def "don't run generated sanity check when test with 'SanityCheck' name already defined"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        def destination = file( 'src/docs/samples/demo')
        def testFile = destination.file("tests/${explicitSanityCheckName}.sample.conf")
        testFile.text = """
            | executable: gradle
            | args: help
            |""".stripMargin()

        buildFile << expectTestsExecuted(getExpectedTestsFor("demo", explicitSanityCheckName));
        build("generateSamplesExemplarFunctionalTest")

        when:
        build('docsTest')
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)

        where:
        explicitSanityCheckName << ["MySanityCheck", "mysanitycheck"]
    }

    def "can test multiple samples"() {
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << createSample('another')
        writeSampleUnderTest(file('src/docs/samples/another'))
        writeExemplarConfigurationToDirectory('src/docs/samples/another')

        buildFile << expectTestsExecuted(getExpectedTestsFor("demo") + getExpectedTestsFor("another"))
        build("generateSamplesExemplarFunctionalTest")

        when:
        build("docsTest")
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
    }

    def "can disable sample testing by renaming the .sample.config file"() {
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << createSample('another')
        writeSampleUnderTest(file('src/docs/samples/another'))
        writeExemplarConfigurationToDirectory('src/docs/samples/another')

        def anotherDemoConfigFile = file('src/docs/samples/another/tests/handWritten.sample.conf')
        anotherDemoConfigFile.text = anotherDemoConfigFile.text.replaceAll('help', 'belp') // make the test fail
        build("generateSamplesExemplarFunctionalTest")

        when:
        buildAndFail("docsTest")

        then:
        result.task(':docsTest').outcome == FAILED

        when:
        assert anotherDemoConfigFile.renameTo(file("src/docs/samples/another/tests/handWritten.sample.confz"))
        build("docsTest")

        then:
        result.task(':docsTest').outcome == SUCCESS
    }

    protected abstract void assertExemplarTasksExecutedAndNotSkipped(BuildResult result)

    protected static void assertExemplarTasksExecutedAndNotSkipped(BuildResult result, String dsl) {
        assert result.task(":installSampleDemo${dsl}").outcome == SUCCESS
        assert result.task(':docsTest').outcome == SUCCESS
    }

    protected void writeExemplarConfigurationToDirectory(String directory = 'src/docs/samples/demo') {
        def destination = file(directory)
        destination.file("tests/handWritten.sample.conf") << getExemplarSampleConfigFileContent()
        destination.file("tests/handWritten.sample.out") << getExemplarSampleOutFileContent()
    }

    protected abstract List<String> getExpectedTestsFor(String sampleName, String... testNames = ["handWritten", "sanityCheck"])

    protected static String expectTestsExecuted(List<String> expected) {
        def script = """
            |task assertTestsExecuted {
            |    ext.tests = []
            |    doLast {
            |        assert tests.size() == ${expected.size()}
            |""".stripMargin()
    expected.each {
        script += "assert tests.contains('${it}')\n"
    }
        script += """
            |    }
            |}
            |
            |tasks.withType(Test).configureEach {
            |    beforeTest { descriptor ->
            |        println("test: " + descriptor)
            |        assertTestsExecuted.tests << descriptor.className + "." + descriptor.name
            |    }
            |    finalizedBy assertTestsExecuted
            |}
            |""".stripMargin()
        return script
    }

    protected static String getExemplarSampleConfigFileContent(String outputFile="handWritten.sample.out") {
        return """
            |commands: [{
            |    executable: gradle
            |    args: help
            |    expected-output-file: ${outputFile}
            |}]
            |""".stripMargin()
    }

    private static String getExemplarSampleOutFileContent() {
        return """
            |> Configure project :
            |Hello, world!
            |
            |> Task :help
            |
            |Welcome to ${GradleVersion.current()}.
            |
            |To run a build, run gradle <task> ...
            |
            |To see a list of available tasks, run gradle tasks
            |
            |To see a list of command-line options, run gradle --help
            |
            |To see more detail about a task, run gradle help --task <task>
            |
            |For troubleshooting, visit https://help.gradle.org
            |
            |BUILD SUCCESSFUL in 0s
            |1 actionable task: 1 executed
            |""".stripMargin()
    }
}
