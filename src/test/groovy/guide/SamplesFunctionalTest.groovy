import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class SamplesFunctionalTest extends Specification {
    private static final File SAMPLES_DIR = new File(System.getProperty('samplesDir'))

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    
    def "can execute plugin dev plugin sample"() {
        given:
        copySample('plugin-dev-plugin')
        
        when:
        build('classes')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute custom task sample"() {
        given:
        copySample('custom-task')
        
        when:
        def result = build('latestVersionMavenCentral', 'latestVersionInhouseRepo')
        
        then:
        result.output.contains("Retrieving artifact commons-lang:commons-lang:1.5 from http://repo1.maven.org/maven2/")
        result.output.contains("Retrieving artifact commons-lang:commons-lang:2.6 from http://my.company.com/maven2")
    }
    
    def "can execute incremental task sample"() {
        given:
        copySample('incremental-task')
        
        when:
        build('generate')
        
        then:
        def outputDir = new File(temporaryFolder.root, 'build/generated-output')
        new File(outputDir, '1.txt').text == 'Hello World!'
        new File(outputDir, '2.txt').text == 'Hello World!'
    }
    
    def "can execute DSL-like API sample"() {
        given:
        copySample('dsl-like-api')
        
        when:
        build('tasks')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute capture user input sample"() {
        given:
        copySample('capture-user-input')
        
        when:
        def result = build('latestArtifactVersion')
        
        then:
        result.output.contains("Retrieving latest artifact version from URL http://my.company.com/maven2")
    }
    
    def "can execute named domain object container sample"() {
        given:
        copySample('named-domain-object-container')
        
        when:
        def result = build('deployToDev', 'deployToStaging', 'deployToProduction')
        
        then:
        result.output.contains("Deploying to URL http://localhost:8080")
        result.output.contains("Deploying to URL http://staging.enterprise.com")
        result.output.contains("Deploying to URL http://prod.enterprise.com")
    }
    
    @Unroll
    def "can execute #description sample"() {
        given:
        copySample(sampleDir)
        
        when:
        build('classes')
        
        then:
        new File(temporaryFolder.root, 'build/classes/java/main/MyClass.class').isFile()
        
        where:
        sampleDir                | description
        'apply-configure-plugin' | 'apply and configure plugin'
        'react-to-plugin'        | 'react to plugin'
    }
    
    @Unroll
    def "can execute react to task sample"() {
        given:
        copySample('react-to-task')
        
        when:
        def result = build('assertWarWebXml')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute default dependency sample"() {
        given:
        copySample('default-dependency')
        
        when:
        def result = build('dependencies')
        
        then:
        result.output.contains("""dataFiles - The data artifacts to be processed for this plugin.
\\--- com.company:more-data:2.6 FAILED""")
    }
    
    def "can execute plugin identifier sample"() {
        given:
        copySample('plugin-identifier')
        
        when:
        def result = build('tasks')
        
        then:
        result.output.contains("Applying Android application plugin")
        result.output.contains("Applying Android library plugin")
    }
    
    private void copySample(String path) {
        FileUtils.copyDirectory(new File(SAMPLES_DIR, "code/$path"), temporaryFolder.root)
    }
    
    private BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }
    
    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        GradleRunner.create().withProjectDir(temporaryFolder.root).withArguments(arguments)
    }
}