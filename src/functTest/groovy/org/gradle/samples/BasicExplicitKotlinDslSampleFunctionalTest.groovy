package org.gradle.samples

class BasicExplicitKotlinDslSampleFunctionalTest extends AbstractKotlinDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            samples.publishedSamples.all {
                dsls = [ Dsl.KOTLIN ]
            }
        """
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
        writeKotlinDslSampleToDirectory(file('src/kotlin-dsl'))
        file('src/kotlin/do.not.include') << "should not be included"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }
}
