package org.gradle.samples

class TestWithExemplarConventionalKotlinDslSampleFunctionalTest extends AbstractExemplarKotlinDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTest(String directory) {
        writeReadmeTo(file(directory))
        writeKotlinDslSample(file(directory))
    }
}
