package org.gradle.docs.samples

import org.gradle.testkit.runner.BuildResult

abstract class AbstractExemplarBothDslSampleFunctionalTest extends AbstractTestWithExemplarSampleFunctionalTest {
    @Override
    protected void assertExemplarTasksExecutedAndNotSkipped(BuildResult result) {
        assertExemplarTasksExecutedAndNotSkipped(result, "Kotlin")
        assertExemplarTasksExecutedAndNotSkipped(result, "Groovy")
    }

    @Override
    protected List<String> getExpectedTestsFor(String sampleName, String... testNames) {
        return testNames.collect { testName -> "org.gradle.samples.ExemplarExternalSamplesFunctionalTest.${sampleName}_groovy_${testName}.sample" } +
                testNames.collect { testName -> "org.gradle.samples.ExemplarExternalSamplesFunctionalTest.${sampleName}_kotlin_${testName}.sample" }
    }
}
