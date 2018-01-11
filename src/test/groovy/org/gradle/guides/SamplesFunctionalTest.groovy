import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    def "can execute convention over configuration sample"() {
        given:
        copySampleCode('convention-over-configuration')

        when:
        def result = succeeds('deploy')

        then:
        result.output.contains('Deploying to URL http://localhost:8080/server')
    }

    def "can execute capabilities vs. conventions sample"() {
        given:
        copySampleCode('capabilities-vs-conventions')

        when:
        def result = succeeds('tasks')

        then:
        noExceptionThrown()
    }

    def "can execute Gradle API sample"() {
        given:
        copySampleCode('gradle-api')

        when:
        succeeds('classes')

        then:
        noExceptionThrown()
    }

    def "can execute external libraries sample"() {
        given:
        copySampleCode('external-libraries')

        when:
        def result = succeeds('buildEnvironment')

        then:
        result.output.contains("""classpath
\\--- org.asciidoctor.gradle.asciidoctor:org.asciidoctor.gradle.asciidoctor.gradle.plugin:1.5.1""")
    }
}
