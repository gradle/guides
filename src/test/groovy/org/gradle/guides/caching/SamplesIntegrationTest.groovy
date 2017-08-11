package org.gradle.guides.caching

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll

@Unroll
class SamplesIntegrationTest extends AbstractSamplesFunctionalTest {

    private static final Map<String, List<String>> TASKS_TO_RUN = [
            'integration-tests'    : ['integTest'],
            'conditional-action'   : ['test'],
            'java-version-tracking': ['test'],
            'reproducible-archives': ['help'],
    ]

    void '#source sample is valid'() {
        copySampleCode(source)
        def tasks = TASKS_TO_RUN[source] ?: ['jar']

        expect:
        succeeds(tasks)

        where:
        source << samples()
    }

    private static List<String> samples() {
        samplesCodeDir.listFiles().findAll { it.isDirectory() }.collect { it.name }
    }
}
