package guides

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files

class ValidateSteps extends Specification {

    static final File SRC_CODE_DIR   = new File( System.getProperty('samplesDir') ?: 'samples', 'code' ).absoluteFile
    static final File SRC_OUTPUT_DIR = new File( System.getProperty('samplesDir') ?: 'samples', 'output' ).absoluteFile

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
    File workingDir

    void setup() {
        workingDir = new File(temporaryFolder.root,'webdemo')
        copyProject()
    }

    void copyProject() {
        Files.copy(new File(SRC_CODE_DIR,'webdemo').toPath(),workingDir.toPath())
    }

    void newBuildScript(final String relativeCodePath) {
        Files.copy(new File(SRC_CODE_DIR,relativeCodePath).toPath(),new File(workingDir,'build.gradle').toPath())
    }

    String build(String... args) {
        List<String> gradleArgs = args as List
        GradleRunner.create().withProjectDir(workingDir).withArguments(gradleArgs).build().getOutput()
    }

    void 'build'() {
        setup:
        newBuildScript('build-3.gradle')

        when:
        String out = build 'build'

        then:
        out.contains('''
:appAfterIntegrationTest
Server stopped.
:check UP-TO-DATE
:build''')
    }
}
