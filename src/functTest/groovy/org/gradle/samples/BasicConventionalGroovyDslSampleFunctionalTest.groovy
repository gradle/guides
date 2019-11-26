package org.gradle.samples

class BasicConventionalGroovyDslSampleFunctionalTest extends AbstractGroovyDslSampleFunctionalTest {
    @Override
    protected void writeSampleUnderTestToDirectory(String directory) {
        writeReadmeTo(file(directory))
        writeGroovyDslSample(file(directory))
    }
}
