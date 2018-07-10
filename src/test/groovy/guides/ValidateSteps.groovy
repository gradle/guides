package guides

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class ValidateSteps extends Specification {

    static final File SRC_CODE_DIR = new File(System.getProperty('samplesDir') ?: 'samples', 'code').absoluteFile
    static final File SRC_OUTPUT_DIR = new File(System.getProperty('samplesDir') ?: 'samples', 'output').absoluteFile

    //TODO(Guy): Try out exemplar for this! It's awesome, I promise :)

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    File workingDir

    void setup() {
        workingDir = temporaryFolder.root
        workingDir.mkdirs()
        new File(workingDir, 'settings.gradle').text = 'rootProject.name="basic-demo"'
    }

    void createBuildScript(final String relativeCodePath, final String scriptName) {
        Files.copy(new File(SRC_CODE_DIR, relativeCodePath).toPath(), new File(workingDir, scriptName).toPath())
    }

    String runGradle(final String task, Iterable<String> args) {
        List<String> gradleArgs = [task]
        if (!args.isEmpty()) gradleArgs.addAll(args)
        gradleArgs.add("--console=rich")
        StringWriter output = new StringWriter()
        GradleRunner.create().forwardStdOutput(output).withProjectDir(workingDir).withArguments(gradleArgs).build()
        output.toString()
    }

    @Unroll
    def 'Run gradle with `#task` (#params) using #scriptName'() {

        setup:
        String expectedOutput = new File(SRC_OUTPUT_DIR, validatorPath).text
        createBuildScript(scriptName, scriptName)
        File src = new File(workingDir, 'src')
        src.mkdirs()
        new File(src, 'myfile.txt').text = 'Hello, world!'

        when:
        params.add("-b")
        params.add(scriptName)
        String output = runGradle task, params

        then:
        output.find(expectedOutput) //contains( new File(SRC_OUTPUT_DIR,validatorPath).text )

        where:
        scriptName                 | task         | params    | validatorPath
        'gradle-properties.gradle' | 'properties' | []        | 'description.txt'
        'gradle-properties.gradle' | 'properties' | []        | 'version.txt'
        'copy.gradle'              | 'tasks'      | ['--all'] | 'gradle-copy-tasks.txt'
        'copy.gradle.kts'          | 'tasks'      | ['--all'] | 'gradle-copy-tasks.txt'
        'copy.gradle'              | 'copy'       | []        | 'gradle-copy.txt'
        'copy.gradle.kts'          | 'copy'       | []        | 'gradle-copy.txt'
        'zip.gradle'               | 'tasks'      | ['--all'] | 'gradle-zip-tasks-1.txt'
        'zip.gradle.kts'           | 'tasks'      | ['--all'] | 'gradle-zip-tasks-1.txt'
        'zip.gradle'               | 'tasks'      | ['--all'] | 'gradle-zip-tasks-2.txt'
        'zip.gradle.kts'           | 'tasks'      | ['--all'] | 'gradle-zip-tasks-2.txt'
    }
}
