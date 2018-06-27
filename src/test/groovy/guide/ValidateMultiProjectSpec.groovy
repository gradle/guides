package guide

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class ValidateMultiProjectSpec extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Unroll
    def 'The example #dsl multi-project must build'() {
        setup:
        FileUtils.copyDirectory(sourceDir, testProjectDir.root)

        when:
        BuildResult result = GradleRunner.create().
            forwardOutput().
            withProjectDir(testProjectDir.root).
            withArguments('-i', '--console=plain', 'build').build()

        then:
        result.task(':greeting-library:build').outcome == TaskOutcome.SUCCESS
//        result.task(':greeter:build').outcome == TaskOutcome.SUCCESS
//        result.task(':docs:build').outcome == TaskOutcome.SUCCESS

        where:
        dsl      | sourceDir
        'groovy' | new File('./src/example/groovy-dsl')
        'kotlin' | new File('./src/example/kotlin-dsl')
    }

}
