package org.gradle.docs.samples

import org.gradle.docs.TestFile
import org.gradle.testkit.runner.BuildResult

class BasicConventionalKotlinDslSampleFunctionalTest extends AbstractKotlinDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(TestFile directory) {
        writeReadmeTo(directory)
        writeKotlinDslSampleTo(directory.file('kotlin'))
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyKotlinDslTasksExecutedAndNotSkipped(result)
    }
}
