package org.gradle.docs.samples

trait SamplesTrait {
    static String createSample(String name) {
        return """
            documentation.samples.publishedSamples.create('${name}')
        """
    }

    static String sampleDsl(String name) {
        return "documentation.samples.publishedSamples.${name}"
    }
}