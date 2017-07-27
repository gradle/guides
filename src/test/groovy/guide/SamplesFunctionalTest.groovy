import spock.lang.Unroll

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    def "can execute plugin dev plugin sample"() {
        given:
        copySampleCode('plugin-dev-plugin')
        
        when:
        succeeds('classes')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute custom task sample"() {
        given:
        copySampleCode('custom-task')
        
        when:
        def result = succeeds('latestVersionMavenCentral', 'latestVersionInhouseRepo')
        
        then:
        result.output.contains("Retrieving artifact commons-lang:commons-lang:1.5 from http://repo1.maven.org/maven2/")
        result.output.contains("Retrieving artifact commons-lang:commons-lang:2.6 from http://my.company.com/maven2")
    }
    
    def "can execute incremental task sample"() {
        given:
        copySampleCode('incremental-task')
        
        when:
        succeeds('generate')
        
        then:
        def outputDir = new File(testDirectory, 'build/generated-output')
        new File(outputDir, '1.txt').text == 'Hello World!'
        new File(outputDir, '2.txt').text == 'Hello World!'
    }
    
    def "can execute DSL-like API sample"() {
        given:
        copySampleCode('dsl-like-api')
        
        when:
        succeeds('tasks')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute capture user input sample"() {
        given:
        copySampleCode('capture-user-input')
        
        when:
        def result = succeeds('latestArtifactVersion')
        
        then:
        result.output.contains("Retrieving latest artifact version from URL http://my.company.com/maven2")
    }
    
    def "can execute named domain object container sample"() {
        given:
        copySampleCode('named-domain-object-container')
        
        when:
        def result = succeeds('deployToDev', 'deployToStaging', 'deployToProduction')
        
        then:
        result.output.contains("Deploying to URL http://localhost:8080")
        result.output.contains("Deploying to URL http://staging.enterprise.com")
        result.output.contains("Deploying to URL http://prod.enterprise.com")
    }
    
    @Unroll
    def "can execute #description sample"() {
        given:
        copySampleCode(sampleDir)
        
        when:
        succeeds('classes')
        
        then:
        new File(testDirectory, 'build/classes/java/main/MyClass.class').isFile()
        
        where:
        sampleDir                | description
        'apply-configure-plugin' | 'apply and configure plugin'
        'react-to-plugin'        | 'react to plugin'
    }
    
    @Unroll
    def "can execute react to task sample"() {
        given:
        copySampleCode('react-to-task')
        
        when:
        def result = succeeds('assertWarWebXml')
        
        then:
        noExceptionThrown()
    }
    
    def "can execute default dependency sample"() {
        given:
        copySampleCode('default-dependency')
        
        when:
        def result = succeeds('dependencies')
        
        then:
        result.output.contains("""dataFiles - The data artifacts to be processed for this plugin.
\\--- com.company:more-data:2.6 FAILED""")
    }
    
    def "can execute plugin identifier sample"() {
        given:
        copySampleCode('plugin-identifier')
        
        when:
        def result = succeeds('tasks')
        
        then:
        result.output.contains("Applying Android application plugin")
        result.output.contains("Applying Android library plugin")
    }
}