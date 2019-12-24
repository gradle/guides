package org.gradle.docs.samples

import org.gradle.testkit.runner.BuildResult

class BasicConventionalGroovyDslSampleFunctionalTest extends AbstractGroovyDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(String directory) {
        writeReadmeTo(file(directory))
        writeGroovyDslSample(file(directory))
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyGroovyDslTasksExecutedAndNotSkipped(result)
    }
}
