package org.gradle.guides.test.fixtures

import spock.lang.Specification

import static org.gradle.guides.test.fixtures.FlawedProjectFixture.*
import static org.gradle.guides.test.fixtures.HelloWorldProjectFixture.failingHelloWorldTask
import static org.gradle.guides.test.fixtures.HelloWorldProjectFixture.successfulHelloWorldTask
import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DefaultFunctionalTestFixtureTest extends Specification {

    final DefaultFunctionalTestFixture fixture = new DefaultFunctionalTestFixture()

    def setup() {
        fixture.initialize()
    }

    def cleanup() {
        fixture.tearDown()
    }

    def "creates basic setup"() {
        expect:
        fixture.gradleRunner
        fixture.testDirectory.isDirectory()
        !new File(fixture.testDirectory, 'build.gradle').exists()
        !new File(fixture.testDirectory, 'settings.gradle').exists()
    }

    def "can create and append to build file"() {
        given:
        String initialBuildFileContent = """
            version = '1.0'
        """
        String laterBuildFileContent = """
            task abc
        """
        when:
        fixture.buildFile << initialBuildFileContent

        then:
        fixture.buildFile.exists()
        fixture.buildFile.text == initialBuildFileContent

        when:
        fixture.buildFile << laterBuildFileContent

        then:
        fixture.buildFile.exists()
        fixture.buildFile.text == initialBuildFileContent + laterBuildFileContent
    }

    def "can create and append to settings file"() {
        given:
        String initialSettingsFileContent = """
            rootProject.name = 'hello'
        """
        String laterSettingsFileContent = """
            include 'a', 'b', 'c'
        """
        when:
        fixture.settingsFile << initialSettingsFileContent

        then:
        fixture.settingsFile.exists()
        fixture.settingsFile.text == initialSettingsFileContent

        when:
        fixture.settingsFile << laterSettingsFileContent

        then:
        fixture.settingsFile.exists()
        fixture.settingsFile.text == initialSettingsFileContent + laterSettingsFileContent
    }

    def "can create new directory"() {
        given:
        String[] path = ['a', 'b', 'c'] as String[]
        File aDir = new File(fixture.testDirectory, 'a')
        File bDir = new File(aDir, 'b')
        File cDir = new File(bDir, 'c')

        when:
        File dir = fixture.dir(path)

        then:
        dir.isDirectory()
        aDir.isDirectory()
        bDir.isDirectory()
        cDir.isDirectory()

        when:
        fixture.dir(path)

        then:
        dir.isDirectory()
        aDir.isDirectory()
        bDir.isDirectory()
        cDir.isDirectory()
    }

    def "can create new file"() {
        given:
        String path = 'a/b/c/some.txt'
        File aDir = new File(fixture.testDirectory, 'a')
        File bDir = new File(aDir, 'b')
        File cDir = new File(bDir, 'c')

        when:
        File file = fixture.file(path)

        then:
        file.isFile()
        aDir.isDirectory()
        bDir.isDirectory()
        cDir.isDirectory()

        when:
        fixture.file(path)

        then:
        file.isFile()
        aDir.isDirectory()
        bDir.isDirectory()
        cDir.isDirectory()
    }

    def "can execute successful build"() {
        given:
        fixture.buildFile << successfulHelloWorldTask()

        when:
        def result = fixture.succeeds('helloWorld')

        then:
        result.task(':helloWorld').outcome == SUCCESS
        result.output.contains('Hello World!')
    }

    def "can execute failing build"() {
        given:
        fixture.buildFile << failingHelloWorldTask()

        when:
        def result = fixture.fails('helloWorld')

        then:
        result.task(':helloWorld').outcome == FAILED
        result.output.contains('expected failure')

        when:
        result = fixture.fails(['helloWorld'])

        then:
        result.task(':helloWorld').outcome == FAILED
        result.output.contains('expected failure')
    }

    def "can configure GradleRunner instance"() {
        given:
        fixture.buildFile << successfulHelloWorldTask()
        StringWriter output = new StringWriter()

        when:
        fixture.gradleRunner.forwardStdOutput(output)
        def result = fixture.succeeds('helloWorld')

        then:
        result.task(':helloWorld').outcome == SUCCESS
        output.toString().contains('Hello World!')

        when:
        fixture.gradleRunner.forwardStdOutput(output)
        result = fixture.succeeds(['helloWorld'])

        then:
        result.task(':helloWorld').outcome == SUCCESS
        output.toString().contains('Hello World!')
    }

    def "throws exception if use of deprecated API is detected"() {
        given:
        def expectedMessage = 'Line 1 contains a deprecation warning: The Task.leftShift(Closure) method has been deprecated and is scheduled to be removed in Gradle 5.0. Please use Task.doLast(Action) instead.'
        fixture.buildFile << deprecatedGradleApiInSuccessfulBuild()

        when:
        fixture.gradleRunner.withGradleVersion('4.0')
        fixture.succeeds('helloWorld')

        then:
        Throwable t = thrown(AssertionError)
        t.message.contains(expectedMessage)

        when:
        fixture.buildFile << deprecatedGradleApiInFailingBuild()
        fixture.fails('byeWorld')

        then:
        t = thrown(AssertionError)
        t.message.contains(expectedMessage)
    }

    def "throws exception if unexpected stack trace is detected"() {
        given:
        def expectedMessage = 'Line 4 contains an unexpected stack trace: \tat sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)'
        fixture.buildFile << unexpectedStackTraceInSuccessfulBuild()

        when:
        fixture.succeeds('helloWorld')

        then:
        Throwable t = thrown(AssertionError)
        t.message.contains(expectedMessage)

        when:
        fixture.buildFile << unexpectedStackTraceInFailingBuild()
        fixture.fails('byeWorld')

        then:
        t = thrown(AssertionError)
        t.message.contains(expectedMessage)
    }
}
