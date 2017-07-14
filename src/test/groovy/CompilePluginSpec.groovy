import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CompilePluginSpec extends Specification {

    @Rule
    TemporaryFolder workspace = new TemporaryFolder()

    def 'Validate example code runs'() {

        setup:
        File output = new File(workspace.root,'output.txt')
        FileUtils.copyDirectory( new File('src/example'), workspace.root)

        when:
        BuildResult runner
        output.withWriter { writer ->
            runner = GradleRunner.create().
                withArguments('hello').
                forwardStdOutput(writer).
                withProjectDir(workspace.root).
                build()
        }

        then:
        runner.task(':hello').outcome == TaskOutcome.SUCCESS
        output.text.contains(':hello')
    }
}
