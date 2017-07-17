package org.gradle.guides.testing

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    File samplesDir

    def setup() {
        samplesDir = dir('samples', 'code')
        System.properties['samplesDir'] = samplesDir.absolutePath
    }

    def cleanup() {
        System.clearProperty('samplesDir')
    }

    def "can execute build and expect successful result"() {
        setup:
        String helloWorldBuildFile = """
task helloWorld {
    doLast {
        println 'Hello World!'
    }
}
"""
        createAndCopyHelloWorldSampleProject(helloWorldBuildFile)

        when:
        def result = succeeds('helloWorld')

        then:
        result.task(':helloWorld').outcome == SUCCESS
        result.output.contains('Hello World!')
    }

    def "can execute build and expect failed result"() {
        setup:
        String helloWorldBuildFile = """
task helloWorld {
    doLast {
        throw new GradleException('expected failure')
    }
}
"""
        createAndCopyHelloWorldSampleProject(helloWorldBuildFile)

        when:
        def result = fails('helloWorld')

        then:
        result.task(':helloWorld').outcome == FAILED
        result.output.contains('expected failure')
    }

    private void createAndCopyHelloWorldSampleProject(String buildFileContent) {
        new File(createSampleDir('code/helloWorld'), 'build.gradle') << buildFileContent
        copySampleCode('helloWorld')
    }

    private File createSampleDir(String path) {
        File sampleDir = new File(samplesDir, path)

        if (!sampleDir.mkdirs()) {
            throw new IOException("Unable to create directory $sampleDir")
        }

        sampleDir
    }
}
