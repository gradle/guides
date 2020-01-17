package org.gradle.docs.samples


import org.gradle.testkit.runner.BuildResult

class BasicExplicitGroovyDslSampleFunctionalTest extends AbstractGroovyDslSampleFunctionalTest {
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

    @Override
    protected void assertSampleTasksExecutedAndNotSkipped(BuildResult result) {
        assertOnlyGroovyDslTasksExecutedAndNotSkipped(result)
    }

    def "can relocate Groovy DSL sample source"() {
        given:
        makeSingleProject()
        buildFile << """
            ${sampleUnderTestDsl} {
                sampleDirectory = file('src')
                groovy {
                    setFrom(file('src/groovy-dsl'))
                }
            }
        """
        writeReadmeTo(file('src'))
        writeGroovyDslSampleTo(file('src/groovy-dsl'))
        file('src/groovy/do.not.include') << "should not be included"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }
}
