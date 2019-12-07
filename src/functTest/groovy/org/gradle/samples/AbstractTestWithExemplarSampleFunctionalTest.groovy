package org.gradle.samples

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

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

${sampleUnderTestDsl}.common { 
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
            samples.create('anotherDemo')
        '''
        writeSampleUnderTestToDirectory('src/samples/anotherDemo')
        writeExemplarConfigurationToDirectory('src/samples/anotherDemo')
        def anotherDemoConfigFile = new File(temporaryFolder.root, 'src/samples/anotherDemo/showDemoSample.sample.conf')
        anotherDemoConfigFile.text = anotherDemoConfigFile.text.replaceAll('help', 'belp') // make the test fail

        when:
        def result1 = buildAndFail("samplesExemplarFunctionalTest")

        then:
        result1.task(':installDemoExemplarSample').outcome == SUCCESS
        result1.task(':samplesExemplarFunctionalTest').outcome == FAILED
        assertExemplarTestExecuted(['demo'], ['anotherDemo'])

        when:
        assert anotherDemoConfigFile.renameTo(new File(temporaryFolder.root, "src/samples/anotherDemo/showDemoSample.sample.confz"))
        def result2 = build("samplesExemplarFunctionalTest")

        then:
        assertExemplarTasksExecutedAndNotSkipped(result2)
        assertExemplarTestSucceeds(['demo'])
    }

    protected abstract void assertExemplarTasksExecutedAndNotSkipped(BuildResult result)

    protected void assertExemplarTasksExecutedAndNotSkipped(BuildResult result, String dsl) {
        assert result.task(":generateSamplesExemplarFunctionalTestSourceSet").outcome == SUCCESS
        assert result.task(":installSampleDemo${dsl}").outcome == SUCCESS
        assert result.task(':samplesExemplarFunctionalTest').outcome == SUCCESS
    }

    protected void writeExemplarConfigurationToDirectory(String directory = 'src/samples/demo') {
        def destination = file(directory)
        destination.file("tests/sanityCheck.sample.conf") << getExemplarSampleConfigFileContent()
        destination.file("tests/sanityCheck.sample.out") << getExemplarSampleOutFileContent()
    }

    protected abstract List<String> getExpectedTestsFor(String sampleName, String testName="sanityCheck")

    protected String expectTestsExecuted(List<String> expected) {
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
         assertTestsExecuted.tests << descriptor.className + "." + descriptor.name
    }
    finalizedBy assertTestsExecuted
}
"""
        return script
    }

    protected String getExemplarSampleConfigFileContent(String outputFile="sanityCheck.sample.out") {
        return """
commands: [{
    executable: gradle
    args: help
    expected-output-file: ${outputFile}
}]
"""
    }

    private String getExemplarSampleOutFileContent() {
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
