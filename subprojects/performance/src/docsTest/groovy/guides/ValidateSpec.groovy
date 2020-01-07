package guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll

import java.nio.file.Files

class ValidateSpec extends AbstractSamplesFunctionalTest {

    static final File SRC_CODE_DIR = new File(System.getProperty('samplesDir') ?: 'samples', 'code')
    static final File SRC_OUTPUT_DIR = new File(System.getProperty('samplesDir') ?: 'samples', 'output')

    void setup() {
        initialize()
    }

    @Unroll
    void 'Ensure performance example works: #source.#scriptExtension'() {
        setup:
        newBuildScript(source, scriptExtension)
        String expectedOutput = new File(SRC_OUTPUT_DIR, "${source}.txt").text

        when:
        String output = runGradle(task)

        then:
        output.contains expectedOutput

        where:
        source             | task                   | scriptExtension
        'copy-1'           | ['tasks', '--all']     | '.gradle'
        'copy-2'           | ['tasks', '--all']     | '.gradle'
        'copy-2'           | ['tasks', '--all']     | '.gradle.kts'
        'copy-3'           | ['--profile', 'help']  | '.gradle'
        'copy-3'           | ['--profile', 'help']  | '.gradle.kts'
        'maxParallelForks' | ['--profile', 'tasks'] | '.gradle'
        'maxParallelForks' | ['--profile', 'tasks'] | '.gradle.kts'
        'compileAvoidance' | ['--profile', 'tasks'] | '.gradle'
        'compileAvoidance' | ['--profile', 'tasks'] | '.gradle.kts'

    }

    private void newBuildScript(final String source, String scriptExtension) {
        Files.copy(new File(SRC_CODE_DIR, "${source}$scriptExtension").toPath(), new File(testDirectory, "build$scriptExtension").toPath())
    }

    private String runGradle(List<String> args) {
        succeeds((String[]) (args.toArray())).output.replaceAll(~/Download.+?\n/, '')
    }

}