package org.gradle.docs.samples

import spock.lang.Unroll

class TestWithExemplarExplicitBothDslSampleFunctionalTest extends AbstractExemplarBothDslSampleFunctionalTest {
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

    @Unroll
    def "tests from tests-#dsl directory are executed in #dsl tests only"() {
        given:
        makeSingleProject()
        writeSampleUnderTest()
        def destination = file( 'src/docs/samples/demo')
        destination.file("tests-${dsl}/mytest.sample.conf").text = """
            | executable: gradle
            | args: help
            |""".stripMargin()
        buildFile << expectTestsExecuted(["org.gradle.samples.ExemplarExternalSamplesFunctionalTest.demo_${dsl}_mytest.sample"] + getExpectedTestsFor('demo', 'sanityCheck'))
        build("generateSamplesExemplarFunctionalTest")
        when:
        build('docsTest')
        then:
        assertExemplarTasksExecutedAndNotSkipped(result)

        where:
        dsl << Dsl.values().collect { it.displayName.toLowerCase() }
    }
}
