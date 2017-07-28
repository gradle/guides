package guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll

import java.nio.file.Files

class ValidateSpec extends AbstractSamplesFunctionalTest {

    static final File SRC_CODE_DIR   = new File( System.getProperty('samplesDir') ?: 'samples', 'code' )
    static final File SRC_OUTPUT_DIR = new File( System.getProperty('samplesDir') ?: 'samples', 'output' )

    void setup() {
        initialize()
    }

    @Unroll
    void 'Ensure performance example works: #source'() {
        setup:
        newBuildScript(source)
        String expectedOutput = new File(SRC_OUTPUT_DIR,"${source}.txt").text

        when:
        String output = runGradle(task)

        then:
        output.contains expectedOutput

        where:
        source             | task
        'copy-1'           | ['tasks','--all']
        'copy-2'           | ['tasks','--all']
        'copy-3'           | ['--profile','help']
        'maxParallelForks' | ['--profile','tasks']
        'compileAvoidance' | ['--profile','tasks']

    }

    private void newBuildScript(final String source) {
        Files.copy(new File(SRC_CODE_DIR,"${source}.gradle").toPath(),new File(testDirectory,'build.gradle').toPath())
    }

    private String runGradle(List<String> args) {
        succeeds((String[])(args.toArray())).output.replaceAll( ~/Download.+?\n/,'')
    }

}