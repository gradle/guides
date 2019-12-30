package org.gradle.docs.samples

class TestWithExemplarExplicitBothDslSampleFunctionalTest extends AbstractExemplarBothDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        Object.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            samples.publishedSamples.all {
                dsls = [ Dsl.KOTLIN, Dsl.GROOVY ]
            }
        """
    }
}
