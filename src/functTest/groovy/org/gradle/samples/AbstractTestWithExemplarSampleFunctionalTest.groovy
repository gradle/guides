package org.gradle.samples

import org.gradle.testkit.runner.BuildResult
import spock.lang.Ignore

import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Ignore
abstract class AbstractTestWithExemplarSampleFunctionalTest extends AbstractSampleFunctionalSpec {
    private static final SKIPPED_TASK_OUTCOMES = [FROM_CACHE, UP_TO_DATE, SKIPPED, NO_SOURCE]

    def "skips install and test if no exemplar sources"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()

        when:
        def result = build('samplesExemplarFunctionalTest')

        then:
        assertExemplarTasksSkipped(result)
    }

    def "can test sample using exemplar"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()

        when:
        def result = build('samplesExemplarFunctionalTest')

        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
        assertExemplarTestSucceeds()
    }

    def "can relocate exemplar configuration"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory('src/exemplar/demo')
        buildFile << """
            ${sampleUnderTestDsl} {
                exemplar.source.from('src/exemplar/demo')
            }
        """

        when:
        def result = build("samplesExemplarFunctionalTest")

        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
        assertExemplarTestSucceeds()
    }
    
    def "can generate exemplar configuration"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()
        buildFile << """
            ${sampleUnderTestDsl} { sample ->
                def generatorTask = tasks.register("generateContentFor\${sample.name.capitalize()}Sample") {
                    outputs.dir(layout.buildDirectory.dir("sample-exemplar/\${sample.name}"))
                    doLast {
                        layout.buildDirectory.dir("sample-exemplar/\${sample.name}/showDemoSample.sample.conf").get().asFile.text = '''${exemplarSampleConfigFileContent}'''
                        layout.buildDirectory.dir("sample-exemplar/\${sample.name}/showDemoSample.sample.out").get().asFile.text = '''${exemplarSampleOutFileContent}'''
                    }
                }
            
                sample.exemplar.source.from(files(generatorTask))
            }
        """

        when:
        def result = build("samplesExemplarFunctionalTest")

        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
        assertExemplarTestSucceeds()
    }

    def "skips (no copy) individual samples if no exemplar sources"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()
        writeSampleUnderTestToDirectory('src/samples/anotherDemo')

        when:
        def result = build('samplesExemplarFunctionalTest')

        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
        assertExemplarTestSucceeds()
        !new File(temporaryFolder.root, 'build/samples-exemplar/anotherDemo').exists()
    }

    def "can generate content for the exemplar functional test"() {
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()
        def exemplarSampleOutFile = new File(temporaryFolder.root, "src/samples/demo/showDemoSample.sample.out")
        exemplarSampleOutFile.text = getExemplarSampleOutFileContent("${expectedPrintlnValue}\nfoobar")
        def kotlinBuildScript = new File(temporaryFolder.root, "src/samples/demo/kotlin/build.gradle.kts")
        def hasKotlinDsl = kotlinBuildScript.exists()
        if (hasKotlinDsl) {
            kotlinBuildScript << '\nprintln(project.getProperties().get("foo.bar"))\n'
        }
        def groovyBuildScript = new File(temporaryFolder.root, "src/samples/demo/groovy/build.gradle")
        def hasGroovyDsl = groovyBuildScript.exists()
        if (hasGroovyDsl) {
            groovyBuildScript << '\nprintln(project.getProperties().get("foo.bar"))\n'
        }
        buildFile << """
            samples.configureEach { sample ->
                def generatorTask = tasks.register("generateContentFor\${sample.name.capitalize()}Sample") {
                    outputs.dir(layout.buildDirectory.dir("sample-contents/\${sample.name}"))
                    doLast {
                        layout.buildDirectory.dir("sample-contents/\${sample.name}/gradle.properties").get().asFile.text = "foo.bar = foobar\\n"
                    }
                }
                sample.archiveContent.from(files(generatorTask))
                
                exemplar.source {
                    ${hasKotlinDsl ? "from(files(generatorTask)) { into('kotlin') }" : ''}
                    ${hasGroovyDsl ? "from(files(generatorTask)) { into('groovy') }" : ''}
                }
            }
        """

        when:
        def result = build("samplesExemplarFunctionalTest")

        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
        assertExemplarTestSucceeds()
        result.task(":generateContentForDemoSample").outcome == SUCCESS
    }

    def "can test multiple samples"() {
        makeSingleProject()
        writeSampleUnderTest()
        writeExemplarConfigurationToDirectory()
        buildFile << '''
            samples.create('anotherDemo')
        '''
        writeSampleUnderTestToDirectory('src/samples/anotherDemo')
        writeExemplarConfigurationToDirectory('src/samples/anotherDemo')

        when:
        def result = build("samplesExemplarFunctionalTest")

        then:
        assertExemplarTasksExecutedAndNotSkipped(result)
        assertExemplarTestSucceeds(['demo', 'anotherDemo'])
    }
    // TODO: Test when the content of an archive is generated

    protected void assertExemplarTestSucceeds(List<String> testCases = ['demo']) {
        assert new File(temporaryFolder.root, "build/reports/tests/samplesExemplarFunctionalTest").exists()

        def testsuiteXmlFile = new File(temporaryFolder.root, "build/test-results/samplesExemplarFunctionalTest/TEST-org.gradle.samples.ExemplarExternalSamplesFunctionalTest.xml")
        assert testsuiteXmlFile.exists()
        def testsuiteNode = new XmlSlurper().parseText(testsuiteXmlFile.text)
        assert testsuiteNode.@name == 'org.gradle.samples.ExemplarExternalSamplesFunctionalTest'
        assert testsuiteNode.@tests == "${testCases.size()}"
        assert testsuiteNode.@skipped == '0'
        assert testsuiteNode.@failures == '0'
        assert testsuiteNode.@errors == '0'

        testsuiteNode.testcase*.@name == testCases.collect { "${it}_showDemoSample.sample" }
    }

    protected void assertExemplarTasksSkipped(BuildResult result) {
        assert result.task(':installDemoExemplarSample').outcome in SKIPPED_TASK_OUTCOMES
        assert result.task(':samplesExemplarFunctionalTest').outcome in SKIPPED_TASK_OUTCOMES
    }

    protected void assertExemplarTasksExecutedAndNotSkipped(BuildResult result) {
        assert result.task(':installDemoExemplarSample').outcome == SUCCESS
        assert result.task(':samplesExemplarFunctionalTest').outcome == SUCCESS
    }

    protected void writeSampleUnderTest() {
        writeSampleUnderTestToDirectory('src/samples/demo')
    }

    protected abstract void makeSingleProject()

    protected abstract void writeSampleUnderTestToDirectory(String directory)

    protected String getSampleUnderTestDsl() {
        return "samples.demo"
    }

    protected void writeExemplarConfigurationToDirectory(String directory = 'src/samples/demo') {
        if (!new File(temporaryFolder.root, directory).exists()) {
            temporaryFolder.newFolder(directory.split('/'))
        }
        temporaryFolder.newFile("${directory}/showDemoSample.sample.conf") << getExemplarSampleConfigFileContent()
        temporaryFolder.newFile("${directory}/showDemoSample.sample.out") << getExemplarSampleOutFileContent()
    }

    protected abstract String getExemplarSampleConfigFileContent()

    private String getExpectedPrintlnValue() {
        return "Hello, world!"
    }

    private String getExemplarSampleOutFileContent(String expectedOutput = expectedPrintlnValue) {
        return """
> Configure project :
${expectedOutput}

> Task :help

Welcome to Gradle 5.5.1.

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
