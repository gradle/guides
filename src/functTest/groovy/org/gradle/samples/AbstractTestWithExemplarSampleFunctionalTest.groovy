package org.gradle.samples

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.FAILED

abstract class AbstractTestWithExemplarSampleFunctionalTest extends AbstractSampleFunctionalSpec {
    def "can test sample using exemplar"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << expectTestsExecuted(getExpectedTestsFor("demo"))

        when:
        build('samplesExemplarFunctionalTest')
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
    }
    
    def "can generate exemplar configuration"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << expectTestsExecuted(getExpectedTestsFor("demo") + getExpectedTestsFor("demo", "otherTest"))

        buildFile << """
abstract class GenerateTask extends DefaultTask {
    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory()
    
    @TaskAction
    void generate() {
        File outputDir = outputDirectory.get().asFile
        new File(outputDir, "otherTest.sample.conf").text = '''${getExemplarSampleConfigFileContent("otherTest.sample.out")}'''
        new File(outputDir, "otherTest.sample.out").text = '''${getExemplarSampleOutFileContent()}'''
    }
}
def generatorTask = tasks.register("generate", GenerateTask) {
    outputDirectory = temporaryDir
}

${sampleUnderTestDsl}.tests { 
    from(generatorTask)
}
        """

        when:
        build("samplesExemplarFunctionalTest")
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
    }

    def "can test multiple samples"() {
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << '''
            samples.publishedSamples.create('another')
        '''
        writeSampleUnderTest('src/samples/another')
        writeExemplarConfigurationToDirectory('src/samples/another')

        buildFile << expectTestsExecuted(getExpectedTestsFor("demo") + getExpectedTestsFor("another"))

        when:
        build("samplesExemplarFunctionalTest")
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
    }

    def "can disable sample testing by renaming the .sample.config file"() {
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        buildFile << '''
            samples.publishedSamples.create('another')
        '''
        writeSampleUnderTest('src/samples/another')
        writeExemplarConfigurationToDirectory('src/samples/another')

        def anotherDemoConfigFile = file('src/samples/another/tests/handWritten.sample.conf')
        anotherDemoConfigFile.text = anotherDemoConfigFile.text.replaceAll('help', 'belp') // make the test fail

        when:
        buildAndFail("samplesExemplarFunctionalTest")

        then:
        result.task(':samplesExemplarFunctionalTest').outcome == FAILED

        when:
        assert anotherDemoConfigFile.renameTo(file("src/samples/another/tests/handWritten.sample.confz"))
        build("samplesExemplarFunctionalTest")

        then:
        result.task(':samplesExemplarFunctionalTest').outcome == SUCCESS
    }

    protected abstract void assertExemplarTasksExecutedAndNotSkipped(BuildResult result)

    protected static void assertExemplarTasksExecutedAndNotSkipped(BuildResult result, String dsl) {
        assert result.task(":generateSamplesExemplarFunctionalTestSourceSet").outcome == SUCCESS
        assert result.task(":installSampleDemo${dsl}").outcome == SUCCESS
        assert result.task(':samplesExemplarFunctionalTest').outcome == SUCCESS
    }

    protected void writeExemplarConfigurationToDirectory(String directory = 'src/samples/demo') {
        def destination = file(directory)
        destination.file("tests/handWritten.sample.conf") << getExemplarSampleConfigFileContent()
        destination.file("tests/handWritten.sample.out") << getExemplarSampleOutFileContent()
    }

    protected abstract List<String> getExpectedTestsFor(String sampleName, String... testNames = ["handWritten", "sanityCheck"])

    protected static String expectTestsExecuted(List<String> expected) {
        def script = """
task assertTestsExecuted {
    ext.tests = []
    doLast {
        assert tests.size() == ${expected.size()}
"""
    expected.each {
        script += "assert tests.contains('${it}')\n"
    }
        script += """
    }
}

tasks.withType(Test).configureEach {
    beforeTest { descriptor ->
        println("test: " + descriptor)
        assertTestsExecuted.tests << descriptor.className + "." + descriptor.name
    }
    finalizedBy assertTestsExecuted
}
"""
        return script
    }

    protected static String getExemplarSampleConfigFileContent(String outputFile="handWritten.sample.out") {
        return """
commands: [{
    executable: gradle
    args: help
    expected-output-file: ${outputFile}
}]
"""
    }

    private static String getExemplarSampleOutFileContent() {
        return """
> Configure project :
Hello, world!

> Task :help

Welcome to ${GradleVersion.current()}.

To run a build, run gradle <task> ...

To see a list of available tasks, run gradle tasks

To see a list of command-line options, run gradle --help

To see more detail about a task, run gradle help --task <task>

For troubleshooting, visit https://help.gradle.org

BUILD SUCCESSFUL in 0s
1 actionable task: 1 executed
"""
    }
}
