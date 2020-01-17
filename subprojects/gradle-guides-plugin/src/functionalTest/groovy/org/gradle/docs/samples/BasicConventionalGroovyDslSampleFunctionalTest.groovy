package org.gradle.docs.samples

import org.gradle.docs.TestFile
import org.gradle.testkit.runner.BuildResult

class BasicConventionalGroovyDslSampleFunctionalTest extends AbstractGroovyDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(TestFile directory) {
        writeReadmeTo(directory)
        writeGroovyDslSampleTo(directory.file('groovy'))
    }

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyGroovyDslTasksExecutedAndNotSkipped(result)
    }
}
