package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

abstract class AbstractExemplarKotlinDslSampleFunctionalTest extends AbstractTestWithExemplarSampleFunctionalTest {
    @Override
    protected void assertExemplarTasksExecutedAndNotSkipped(BuildResult result) {
        assertExemplarTasksExecutedAndNotSkipped(result, "Kotlin")
    }

    @Override
    protected List<String> getExpectedTestsFor(String sampleName, String testName = "sanityCheck") {
        return [
                "org.gradle.samples.ExemplarExternalSamplesFunctionalTest.${sampleName}_kotlin_${testName}.sample"
        ]
    }
}
