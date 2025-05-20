package org.gradle.docs.samples

import org.gradle.docs.TestFile

class TestWithExemplarConventionalKotlinDslSampleFunctionalTest extends AbstractExemplarKotlinDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(TestFile directory) {
        writeReadmeTo(directory)
        writeKotlinDslSampleTo(directory.file('kotlin'))
    }
}
