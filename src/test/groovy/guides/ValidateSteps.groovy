package guides

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class ValidateSteps extends Specification {

    static final File SRC_CODE_DIR   = new File( System.getProperty('samplesDir') ?: 'samples', 'code' )
    static final File SRC_OUTPUT_DIR = new File( System.getProperty('samplesDir') ?: 'samples', 'output' )

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
    File workingDir

    void setup() {
        workingDir = temporaryFolder.root
        new File(workingDir,'settings.gradle').text=''
    }

    void createBuildScript(final String relativeCodePath) {
        Files.copy(new File(SRC_CODE_DIR,relativeCodePath).toPath(),new File(workingDir,'build.gradle').toPath())
    }

    String runGradle(final String task,Iterable<String> args) {
        List<String> gradleArgs = [task]
        gradleArgs.addAll(args)
        StringWriter output = new StringWriter()
        GradleRunner.create().forwardStdOutput(output).withProjectDir(workingDir).withArguments(gradleArgs).build()
        output.toString()
    }

    @Unroll
    def 'Run gradle with #task (#params) using #scriptName'() {

        setup:
        createBuildScript( scriptName )

        when:
        String output = runGradle task, params

        then:
        output.contains( new File(SRC_OUTPUT_DIR,validatorPath).text )

        where:
        scriptName                  | task       | params    | validatorPath
        'part1/custom-hello.gradle' | 'tasks'    | ['--all'] | 'part1/gradle-tasks.txt'
        'part1/custom-hello.gradle' | 'tasks'    | ['--all'] | 'part1/gradle-tasks-setup.txt'
        'part1/custom-hello.gradle' | 'hello'    | []        | 'part1/gradle-hello.txt'
        'part2/custom-hello.gradle' | 'tasks'    | []        | 'part2/gradle-tasks.txt'
        'part3/custom-hello.gradle' | 'hello'    | []        | 'part3/gradle-hello.txt'
        'part3/german-hello.gradle' | 'tasks'    | []        | 'part3/gradle-tasks.txt'
        'part3/german-hello.gradle' | 'gutenTag' | []        | 'part3/gradle-gutentag.txt'
    }
}
