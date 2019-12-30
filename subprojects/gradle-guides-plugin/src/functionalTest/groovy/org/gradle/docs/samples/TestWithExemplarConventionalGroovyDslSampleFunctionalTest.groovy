package org.gradle.docs.samples

class TestWithExemplarConventionalGroovyDslSampleFunctionalTest extends AbstractExemplarGroovyDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(String directory) {
        writeReadmeTo(file(directory))
        writeGroovyDslSample(file(directory))
    }
}
