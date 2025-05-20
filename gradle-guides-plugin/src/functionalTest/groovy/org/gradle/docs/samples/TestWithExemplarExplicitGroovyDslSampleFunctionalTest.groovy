package org.gradle.docs.samples

class TestWithExemplarExplicitGroovyDslSampleFunctionalTest extends AbstractExemplarGroovyDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            documentation.samples.publishedSamples.all {
                dsls = [ Dsl.GROOVY ]
            }
        """
    }
}
