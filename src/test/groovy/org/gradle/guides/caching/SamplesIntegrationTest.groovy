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

    @Unroll
    void '#testData.source sample using #testData.scriptFileName is valid'() {
        copySampleCode(testData.source)
        def tasks = TASKS_TO_RUN[testData.source] ?: ['jar']

        expect:
        succeeds(['-b', testData.scriptFileName] + tasks)

        where:
        testData << createTestData()
    }

    static class TestData {
        String source
        String scriptFileName
    }

    private static List<TestData> createTestData() {
        sampleDirs().collectMany { dir ->
            ['build.gradle', 'build.gradle.kts'].collect { scriptFileName ->
                def script = new File(dir, scriptFileName)
                if (script.isFile()) new TestData(source: dir.name, scriptFileName: scriptFileName)
                else null
            }.findAll { it != null }
        }
    }

    private static List<File> sampleDirs() {
        samplesCodeDir.listFiles().findAll { it.isDirectory() }
    }
}
