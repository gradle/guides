package org.gradle.docs.samples

import org.gradle.docs.TestFile

class TestWithExemplarConventionalGroovyDslSampleFunctionalTest extends AbstractExemplarGroovyDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(TestFile directory) {
        writeReadmeTo(directory)
        writeGroovyDslSampleTo(directory.file('groovy'))
    }
}
