import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SamplesFunctionalTest extends Specification {
    private static final File SAMPLES_DIR = new File(System.getProperty('samplesDir'))

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    
    def "can execute convention over configuration sample"() {
        given:
        copySample('convention-over-configuration')
        
        when:
        def result = build('deploy')
        
        then:
        result.output.contains('Deploying to URL http://localhost:8080/server')
    }
    
    def "can execute capabilities vs. conventions sample"() {
        given:
        copySample('capabilities-vs-conventions')
        
        when:
        def result = build('tasks')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute Gradle API sample"() {
        given:
        copySample('gradle-api')
        
        when:
        build('classes')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute external libraries sample"() {
        given:
        copySample('external-libraries')
        
        when:
        def result = build('buildEnvironment')
        
        then:
        result.output.contains("""classpath
\\--- org.asciidoctor:asciidoctor-gradle-plugin:1.5.1""")
    }
    
    private void copySample(String path) {
        FileUtils.copyDirectory(new File(SAMPLES_DIR, "code/$path"), temporaryFolder.root)
    }
    
    private BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }
    
    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        GradleRunner.create().withProjectDir(temporaryFolder.root).withArguments(arguments).forwardOutput()
    }
}