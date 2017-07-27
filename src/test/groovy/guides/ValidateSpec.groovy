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
    void 'Ensure migration example works: #source'() {
        setup:
        newBuildScript(source)
        String expectedOutput = new File(SRC_OUTPUT_DIR,"${source}.txt").text

        when:
        String output = runGradle(task)

        then:
        output.contains expectedOutput

        where:
        source             | task
        'bom'              | ['dependencies','--console=plain']
        'provided'         | ['dependencies']
        'processResources' | ['processResources']
        'integTest'        | ['tasks']
        'checkstyle'       | ['tasks','--all']
        'ant'              | ['sayHello']

    }

    private void newBuildScript(final String source) {
        Files.copy(new File(SRC_CODE_DIR,"${source}.gradle").toPath(),new File(testDirectory,'build.gradle').toPath())
    }

    private String runGradle(List<String> args) {
        succeeds((String[])(args.toArray())).output.replaceAll( ~/Download.+?\n/,'')
    }

}