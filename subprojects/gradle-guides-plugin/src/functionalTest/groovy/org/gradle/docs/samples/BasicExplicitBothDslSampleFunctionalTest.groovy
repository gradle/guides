package org.gradle.docs.samples

class BasicExplicitBothDslSampleFunctionalTest extends AbstractBothDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            documentation.samples.publishedSamples.all {
                dsls = [ Dsl.KOTLIN, Dsl.GROOVY ]
            }
        """
    }

    def "can relocate both DSL sample source"() {
        given:
        makeSingleProject()
        buildFile << """
            ${sampleUnderTestDsl} {
                sampleDirectory = file('src')
                groovy {
                    setFrom(file('src/groovy-dsl'))
                }
                kotlin {
                    setFrom(file('src/kotlin-dsl'))
                }
            }
        """
        writeReadmeTo(file('src'))
        writeGroovyDslSampleTo(file('src/groovy-dsl'))
        file('src/groovy/do.not.include') << "should not be included"

        writeKotlinDslSampleTo(file('src/kotlin-dsl'))
        file('src/kotlin/do.not.include') << "should not be included"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }
}
