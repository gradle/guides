package org.gradle.guides

import spock.lang.Unroll

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    private static final String BUILD_SCAN_PUBLISHED_MESSAGE = 'Publishing build scan...'

    def "can create build scan from auto-applied plugin"() {
        given:
        copySampleCode('auto-applied-build-scan-plugin')

        when:
        def input = new ByteArrayInputStream(('yes' + System.getProperty('line.separator')).bytes)
        System.setIn(input)
        gradleRunner.withStandardInput(System.in)
        def result = succeeds('build', '--scan')

        then:
        result.task(':build').outcome == UP_TO_DATE
        result.output.contains(BUILD_SCAN_PUBLISHED_MESSAGE)
    }

    @Unroll
    def "can create build scan from #lang build script"() {
        given:
        copySampleCode('build-scan-from-build-script')

        when:
        def result = succeeds('-b', buildScriptFilename, 'build', '--scan')

        then:
        result.task(':build').outcome == UP_TO_DATE
        result.output.contains(BUILD_SCAN_PUBLISHED_MESSAGE)

        where:
        lang     | buildScriptFilename
        'Groovy' | 'build.gradle'
        'Kotlin' | 'build.gradle.kts'
    }

    @Unroll
    def "can create build scan from #lang init script"() {
        given:
        copySampleCode('build-scan-from-init-script')

        when:
        def result = succeeds('-b', buildScriptFilename, 'build', '--scan', '-I', initScriptFilename)

        then:
        result.task(':build').outcome == UP_TO_DATE
        result.output.contains(BUILD_SCAN_PUBLISHED_MESSAGE)

        where:
        lang     | buildScriptFilename | initScriptFilename
        'Groovy' | 'build.gradle'      | 'buildScan.gradle'
        'Kotlin' | 'build.gradle.kts'  | 'buildScan.init.gradle.kts'
    }
}
