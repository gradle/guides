package guide

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ValidateMultiProjectSpec extends Specification {

    static final File SOURCE_DIR = new File( './src/example')

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    def 'The example multi-project must build'() {
        setup:
        FileUtils.copyDirectory(SOURCE_DIR,testProjectDir.root)

        when:
        BuildResult result = GradleRunner.create().
            forwardOutput().
            withProjectDir(testProjectDir.root).
            withArguments('-i', '--console=plain','build').build()

        then:
        result.task(':greeting-library:build').outcome == TaskOutcome.SUCCESS
//        result.task(':greeter:build').outcome == TaskOutcome.SUCCESS
//        result.task(':docs:build').outcome == TaskOutcome.SUCCESS


    }

}
