package org.gradle.samples

class BasicExplicitGroovyDslSampleFunctionalTest extends AbstractGroovyDslSampleFunctionalTest {
    @Override
    protected void makeSingleProject() {
        super.makeSingleProject()
        buildFile << """
            import ${Dsl.canonicalName}
            samples.publishedSamples.all {
                dsls = [ Dsl.GROOVY ]
            }
        """
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
        writeGroovyDslSampleToDirectory(file('src/groovy-dsl'))
        file('src/groovy/do.not.include') << "should not be included"

        when:
        build('assembleDemoSample')

        then:
        assertSampleTasksExecutedAndNotSkipped(result)
        assertDslZipsHaveContent()
    }
}
