package org.gradle.samples

class TestWithExemplarExplicitBothDslSampleFunctionalTest extends AbstractExemplarBothDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            samples.publishedSamples.all {
                dsls = [ Dsl.KOTLIN, Dsl.GROOVY ]
            }
        """
    }
}
