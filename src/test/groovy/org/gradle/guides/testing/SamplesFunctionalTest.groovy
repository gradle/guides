package org.gradle.guides.testing

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    private static final String SAMPLE_CODE_PROJECT_DIR_NAME = 'use-case'

    @Rule
    final TemporaryFolder samplesDirFolder = new TemporaryFolder()

    private File samplesCodeDir
    private File samplesBuildFile

    def setup() {
        samplesCodeDir = samplesDirFolder.newFolder('code', SAMPLE_CODE_PROJECT_DIR_NAME)
        samplesBuildFile = new File(samplesCodeDir, 'build.gradle')
        System.properties['samplesDir'] = samplesDirFolder.root.absolutePath
    }

    def cleanup() {
        System.clearProperty('samplesDir')
    }

    def "can execute build and expect successful result"() {
        given:
        samplesBuildFile << """
task helloWorld {
    doLast {
        println 'Hello World!'
    }
}
"""
        copySampleCode(SAMPLE_CODE_PROJECT_DIR_NAME)

        when:
        def result = succeeds('helloWorld')

        then:
        result.task(':helloWorld').outcome == SUCCESS
        result.output.contains('Hello World!')
    }

    def "can execute build and expect failed result"() {
        setup:
        samplesBuildFile << """
task helloWorld {
    doLast {
        throw new GradleException('expected failure')
    }
}
"""
        copySampleCode(SAMPLE_CODE_PROJECT_DIR_NAME)

        when:
        def result = fails('helloWorld')

        then:
        result.task(':helloWorld').outcome == FAILED
        result.output.contains('expected failure')
    }

    def "can copy sample directory recursively"() {
        setup:
        samplesBuildFile << """
apply plugin: 'java'

repositories {
    jcenter()
}

dependencies {
    testCompile 'junit:junit:4.12'
}
"""
        File javaSrcDir = new File(samplesCodeDir, 'src/main/java/com/company')
        javaSrcDir.mkdirs()
        new File(javaSrcDir, 'MyClass.java') << """
package com.company;

public class MyClass {}
"""
        copySampleCode(SAMPLE_CODE_PROJECT_DIR_NAME)

        when:
        def result = succeeds('compileJava')

        then:
        result.task(':compileJava').outcome == SUCCESS
        new File(testDirectory, 'build/classes/java/main/com/company/MyClass.class').exists()
    }
}
