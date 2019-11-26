package org.gradle.samples

class BasicConventionalKotlinDslSampleFunctionalTest extends AbstractKotlinDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTestToDirectory(String directory) {
        writeReadmeTo(file(directory))
        writeKotlinDslSample(file(directory))
    }
}
