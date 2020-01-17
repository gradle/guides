package org.gradle.docs.samples

import org.gradle.testkit.runner.BuildResult

abstract class AbstractExemplarGroovyDslSampleFunctionalTest extends AbstractTestWithExemplarSampleFunctionalTest {
    @Override
    protected void assertExemplarTasksExecutedAndNotSkipped(BuildResult result) {
        assertExemplarTasksExecutedAndNotSkipped(result, "Groovy")
    }

    @Override
    protected List<String> getExpectedTestsFor(String sampleName, String... testNames) {
        return testNames.collect { testName -> "org.gradle.samples.ExemplarExternalSamplesFunctionalTest.${sampleName}_groovy_${testName}.sample".toString() }
    }
}
