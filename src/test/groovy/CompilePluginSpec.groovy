import org.apache.commons.io.FileUtils
import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class CompilePluginSpec extends AbstractSamplesFunctionalTest {

    static final File SRC_CODE_DIR   = new File( System.getProperty('samplesDir') ?: 'samples', 'code' )
    static final File SRC_OUTPUT_DIR = new File( System.getProperty('samplesDir') ?: 'samples', 'output' )

    void setup() {
        initialize()
    }

    void 'Validate example code runs'() {

        setup:
        FileUtils.copyDirectory( SRC_CODE_DIR, testDirectory)

        when:
        String output = runGradle 'hello'

        then:
        output.contains(':hello')
        output.contains('Hello, World')
    }

    private String runGradle(String... args) {
        succeeds(args).output.replaceAll( ~/Download.+?\n/,'')
    }
}
