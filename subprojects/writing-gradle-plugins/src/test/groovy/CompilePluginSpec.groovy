import spock.lang.Unroll

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

    @Unroll
    void 'Validate example code runs with #lang'() {

        setup:
        FileUtils.copyDirectory(SRC_CODE_DIR, testDirectory)

        when:
        def output = runGradle('-b', buildScriptFilename, 'hello')

        then:
        output.contains(':hello')
        output.contains('Hello, World')

        where:
        lang     | buildScriptFilename
        'Groovy' | 'build.gradle'
        'Kotlin' | 'build.gradle.kts'
    }

    @Unroll
    void 'Validate configure task runs with #lang'() {

        setup:
        FileUtils.copyDirectory(SRC_CODE_DIR, testDirectory)

        when:
        def output = runGradle('-b', buildScriptFilename, 'hello')

        then:
        output.contains(':hello')
        output.contains('Hi, Gradle')

        where:
        lang     | buildScriptFilename
        'Groovy' | 'configure-hello.gradle'
        'Kotlin' | 'configure-hello.gradle.kts'
    }

    private String runGradle(String... args) {
        succeeds(args).output.replaceAll( ~/Download.+?\n/,'')
    }
}
