package org.gradle.docs.samples


import org.gradle.testkit.runner.BuildResult

class BasicExplicitKotlinDslSampleFunctionalTest extends AbstractKotlinDslSampleFunctionalTest {
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

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyKotlinDslTasksExecutedAndNotSkipped(result)
    }

    def "can relocate Kotlin DSL sample source"() {
        given:
        makeSingleProject()
        buildFile << """
            ${sampleUnderTestDsl} {
                sampleDirectory = file('src')
                kotlin {
                    setFrom(file('src/kotlin-dsl'))
                }
            }
        """
        writeReadmeTo(file('src'))
        writeKotlinDslSampleTo(file('src/kotlin-dsl'))
        file('src/kotlin/do.not.include') << "should not be included"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }
}
