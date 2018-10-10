package org.gradle.guides

import spock.lang.Unroll

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    @Unroll
    def "can execute plugin dev plugin sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/plugin-dev-plugin")

        when:
        succeeds('classes')

        then:
        noExceptionThrown()

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
    }

    @Unroll
    def "can execute custom task sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/custom-task")

        when:
        def result = succeeds('latestVersionMavenCentral', 'latestVersionInhouseRepo')

        then:
        result.output.contains("Retrieving artifact commons-lang:commons-lang:1.5 from http://repo1.maven.org/maven2/")
        result.output.contains("Retrieving artifact commons-lang:commons-lang:2.6 from http://my.company.com/maven2")

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
    }

    @Unroll
    def "can execute incremental task sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/incremental-task")

        when:
        succeeds('generate')

        then:
        def outputDir = new File(testDirectory, 'build/generated-output')
        new File(outputDir, '1.txt').text == 'Hello World!'
        new File(outputDir, '2.txt').text == 'Hello World!'

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
    }

    @Unroll
    def "can execute DSL-like API sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/dsl-like-api")

        when:
        succeeds('tasks')

        then:
        noExceptionThrown()

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
    }

    @Unroll
    def "can execute capture user input sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/capture-user-input")

        when:
        def result = succeeds('latestArtifactVersion')

        then:
        result.output.contains("Retrieving latest artifact version from URL http://my.company.com/maven2")

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
    }

    @Unroll
    def "can execute named domain object container sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/named-domain-object-container")

        when:
        def result = succeeds('deployToDev', 'deployToStaging', 'deployToProduction')

        then:
        result.output.contains("Deploying to URL http://localhost:8080")
        result.output.contains("Deploying to URL http://staging.enterprise.com")
        result.output.contains("Deploying to URL http://prod.enterprise.com")

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
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

    @Unroll
    def "can execute default dependency sample with the #dsl"() {
        given:
        copySampleCode("../$location/code/default-dependency")

        when:
        def result = succeeds('dependencies')

        then:
        result.output.contains("""dataFiles - The data artifacts to be processed for this plugin.
\\--- com.company:more-data:2.6 FAILED""")

        where:
        dsl          | location
        'kotlin-dsl' | 'kotlin-dsl'
        'groovy-dsl' | 'groovy-dsl'
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
