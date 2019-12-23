package guide

import org.gradle.testkit.runner.UnexpectedBuildFailure
import spock.lang.Unroll

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class PublishSpec extends Specification {

    static final int PORT = System.getProperty('HTTPSERVER_PORT')?.toInteger() ?: 65000
    static final File PROJECT_TEMPLATE_DIR = new File(System.getProperty('samplesDir') ?: 'samples')
    static final File PLUGIN_CLASSPATH = new File( System.getProperty('PLUGIN_CLASSPATH') ?: 'build/pluginClasspath')

    @Shared SimpleHttpServer server = new SimpleHttpServer(PORT,'/')
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    void setupSpec() {
        server.start()
    }

    void cleanupSpec() {
        server.stop()
    }

    @Unroll
    def 'Publish plugin to portal using fake server with #lang'() {
        given:
        FileUtils.copyFileToDirectory(new File(PROJECT_TEMPLATE_DIR, buildScriptFilename), testProjectDir.root)
        testProjectDir.newFile("settings.gradle").text = ""
        testProjectDir.newFile("gradle.properties").text = """
            gradle.publish.key=foo
            gradle.publish.secret=bar
            systemProp.gradle.portal.url=http://localhost:${PORT}
        """

        when:
        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('publishPlugin', '--stacktrace')
            .withPluginClasspath(PLUGIN_CLASSPATH.listFiles() as List)
            .forwardOutput()
            .build()

        then: 'Exception is thrown due to invalid plugin ID. This is good for this test'
        def failure = thrown(UnexpectedBuildFailure.class)
        failure.buildResult.output.contains("Invalid plugin ID")

        where:
        lang     | buildScriptFilename
        'Groovy' | 'build.gradle'
        'Kotlin' | 'build.gradle.kts'
    }
}
