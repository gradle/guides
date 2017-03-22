package guide

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class PublishSpec extends Specification {

    static final int PORT = System.getProperty('HTTPSERVER_PORT')?.toInteger() ?: 65000
    static final File PROJECT_TEMPLATE_DIR = new File(System.getProperty('TEMPLATE_DIR') ?: 'src/publicationTest')
    static final File PLUGIN_CLASSPATH = new File( System.getProperty('PLUGIN_CLASSPATH') ?: 'build/pluginClasspath')

    @Shared SimpleHttpServer server = new SimpleHttpServer(PORT,'/')
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    void setupSpec() {
        server.start()
    }

    void cleanupSpec() {
        server.stop()
    }

    def 'Publish plugin to portal using fake server'() {

        setup: 'Copy publication tree to temporary folder'
        FileUtils.copyDirectory(PROJECT_TEMPLATE_DIR,testProjectDir.root)
        File buildFile = new File(testProjectDir.root,'build.gradle')

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("-Dgradle.portal.url=http://localhost:${PORT}",'-u','jar','publishPlugin','-Pgradle.publish.key=foo','-Pgradle.publish.secret=bar','-s')
            .withPluginClasspath(PLUGIN_CLASSPATH.listFiles() as List)
            .forwardOutput()
            .build()

        then: 'Exception is thrown due to invalid JSON. This is good for this test'
        thrown(java.lang.RuntimeException)
    }
}
