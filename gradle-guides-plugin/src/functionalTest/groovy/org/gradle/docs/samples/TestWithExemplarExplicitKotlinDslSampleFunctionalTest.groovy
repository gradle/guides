package org.gradle.docs.samples

class TestWithExemplarExplicitKotlinDslSampleFunctionalTest extends AbstractExemplarKotlinDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            documentation.samples.publishedSamples.all {
                dsls = [ Dsl.KOTLIN ]
            }
        """
    }
}
