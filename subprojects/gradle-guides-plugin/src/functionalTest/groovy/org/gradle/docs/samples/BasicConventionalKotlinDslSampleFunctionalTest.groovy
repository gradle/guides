package org.gradle.docs.samples

import org.gradle.testkit.runner.BuildResult

class BasicConventionalKotlinDslSampleFunctionalTest extends AbstractKotlinDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(String directory) {
        writeReadmeTo(file(directory))
        writeKotlinDslSample(file(directory))
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyKotlinDslTasksExecutedAndNotSkipped(result)
    }
}
