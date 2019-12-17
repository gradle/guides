package guides

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class ValidateSteps extends Specification {

    static final File SAMPLES_DIR = new File( System.getProperty('samplesDir') ?: 'samples')

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
    File workingDir

    void setup() {
        workingDir = temporaryFolder.root
    }

    void createBuildScript(final String codeDir, final String relativeCodePath, extension) {
        Files.copy(new File(codeDir, relativeCodePath).toPath(), new File(workingDir,"build.${extension}").toPath())
    }

    String runGradle(final String task, Iterable<String> args) {
        List<String> gradleArgs = [task]
        gradleArgs.addAll(args)
        GradleRunner.create().withProjectDir(workingDir).withArguments(gradleArgs).build().output
    }

    @Unroll
    def 'Run gradle with #task (#params) using #scriptName'() {

        setup:
        String codeDir = new File(new File(SAMPLES_DIR, dsl), 'code')
        String outputDir = new File(new File(SAMPLES_DIR, dsl), 'output')
        createBuildScript(codeDir, scriptName, extension)

        when:
        String output = runGradle task, params

        then:
        output.contains(new File(outputDir, validatorPath).text)

        where:
        dsl          | extension     | scriptName                      | task       | params    | validatorPath
        'groovy-dsl' | 'gradle'      | 'part1/custom-hello.gradle'     | 'tasks'    | ['--all'] | 'part1/gradle-tasks.txt'
        'groovy-dsl' | 'gradle'      | 'part1/custom-hello.gradle'     | 'tasks'    | ['--all'] | 'part1/gradle-tasks-setup.txt'
        'groovy-dsl' | 'gradle'      | 'part1/custom-hello.gradle'     | 'hello'    | []        | 'part1/gradle-hello.txt'
        'groovy-dsl' | 'gradle'      | 'part2/custom-hello.gradle'     | 'tasks'    | []        | 'part2/gradle-tasks.txt'
        'groovy-dsl' | 'gradle'      | 'part3/custom-hello.gradle'     | 'hello'    | []        | 'part3/gradle-hello.txt'
        'groovy-dsl' | 'gradle'      | 'part3/german-hello.gradle'     | 'tasks'    | []        | 'part3/gradle-tasks.txt'
        'groovy-dsl' | 'gradle'      | 'part3/german-hello.gradle'     | 'gutenTag' | []        | 'part3/gradle-gutentag.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part1/custom-hello.gradle.kts' | 'tasks'    | ['--all'] | 'part1/gradle-tasks.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part1/custom-hello.gradle.kts' | 'tasks'    | ['--all'] | 'part1/gradle-tasks-setup.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part1/custom-hello.gradle.kts' | 'hello'    | []        | 'part1/gradle-hello.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part2/custom-hello.gradle.kts' | 'tasks'    | []        | 'part2/gradle-tasks.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part3/custom-hello.gradle.kts' | 'hello'    | []        | 'part3/gradle-hello.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part3/german-hello.gradle.kts' | 'tasks'    | []        | 'part3/gradle-tasks.txt'
        'kotlin-dsl' | 'gradle.kts'  | 'part3/german-hello.gradle.kts' | 'gutenTag' | []        | 'part3/gradle-gutentag.txt'
    }
}
