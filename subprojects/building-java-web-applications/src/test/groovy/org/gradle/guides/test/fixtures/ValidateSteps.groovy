package org.gradle.guides.test.fixtures

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files

class ValidateSteps extends Specification {

    static final File SAMPLES_DIR = new File(System.getProperty('samplesDir') ?: 'samples').absoluteFile

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
    File workingDir

    void setup() {
        workingDir = new File(temporaryFolder.root,'webdemo')
    }

    void copyProject(File sourceDir) {
        Files.copy(new File(sourceDir, 'webdemo').toPath(), workingDir.toPath())
    }

    void newBuildScript(File sourceDir, final String relativeCodePath, String buildScriptExtension) {
        Files.copy(new File(sourceDir, "${relativeCodePath}${buildScriptExtension}").toPath(),
                   new File(workingDir, "build.gradle${buildScriptExtension}").toPath())
    }

    String build(String... args) {
        List<String> gradleArgs = args as List
        GradleRunner.create().withProjectDir(workingDir).withArguments(gradleArgs).build().getOutput()
    }

    def 'build output is as expected using #dsl DSL'() {
        setup:
        copyProject(sourceDir)
        newBuildScript(sourceDir, 'build-3.gradle', extension)

        when:
        String out = build 'build'

        then:
        out.contains('''> Task :appAfterIntegrationTest
Server stopped.''')

        where:
        dsl      | sourceDir                                 | extension
        'groovy' | new File(SAMPLES_DIR, 'groovy-dsl') | ''
        'kotlin' | new File(SAMPLES_DIR, 'kotlin-dsl') | '.kts'
    }
}
