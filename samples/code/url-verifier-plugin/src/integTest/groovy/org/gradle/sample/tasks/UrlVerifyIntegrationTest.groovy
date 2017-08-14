package org.gradle.sample.tasks

import org.gradle.api.GradleException
import org.gradle.sample.http.HttpCallException
import org.gradle.sample.http.HttpCaller
import org.gradle.sample.http.HttpResponse
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

class UrlVerifyIntegrationTest extends Specification {
    private static final String EXAMPLE_URL = 'http://www.google.com/'

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
    @Subject UrlVerify verifyUrlTask
    def httpCaller = Mock(HttpCaller)

    def setup() {
        def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        verifyUrlTask = project.tasks.create('verifyUrl', UrlVerify)
        verifyUrlTask.httpCaller = httpCaller
        verifyUrlTask.url = EXAMPLE_URL
    }

    def "can successfully resolve URL"() {
        when:
        verifyUrlTask.verify()

        then:
        1 * httpCaller.get(EXAMPLE_URL) >> new HttpResponse(200, 'OK')
    }

    def "throws exception when resolving URL for status code other than 200"() {
        when:
        verifyUrlTask.verify()

        then:
        1 * httpCaller.get(EXAMPLE_URL) >> new HttpResponse(500, 'Internal Server Error')
        def t = thrown(GradleException)
        t.message == "Failed to resolve url 'http://www.google.com/' (HTTP 500, Reason: Internal Server Error)"
    }

    def "rethrows exception when failing to resolve URL"() {
        when:
        verifyUrlTask.verify()

        then:
        1 * httpCaller.get(EXAMPLE_URL) >> { throw new HttpCallException('unknown host') }
        def t = thrown(GradleException)
        t.message == "Failed to resolve url 'http://www.google.com/'"
    }
}
