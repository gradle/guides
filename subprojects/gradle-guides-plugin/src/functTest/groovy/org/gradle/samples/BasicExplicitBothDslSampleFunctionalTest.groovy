package org.gradle.samples

class BasicExplicitBothDslSampleFunctionalTest extends AbstractBothDslSampleFunctionalTest {
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
        writeGroovyDslSampleToDirectory(file('src/groovy-dsl'))
        file('src/groovy/do.not.include') << "should not be included"

        writeKotlinDslSampleToDirectory(file('src/kotlin-dsl'))
        file('src/kotlin/do.not.include') << "should not be included"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }
}
