package org.gradle.plugins.site

class SitePluginFunctionalTest extends AbstractFunctionalTest {

    def "can generate site for default conventions"() {
        when:
        build('site')

        then:
        def indexFile = new File(projectDir,'build/docs/site/index.html')
        indexFile.isFile()
        indexFile.text == projectDir.name
    }

    def "can generate site for custom conventions"() {
        given:
        def customOutputDir = 'build/custom/site'
        buildFile << """
            site {
                outputDir = file('$customOutputDir')
            }
        """

        when:
        build('site')

        then:
        def indexFile = new File(projectDir, "$customOutputDir/index.html")
        indexFile.isFile()
        indexFile.text == projectDir.name
    }
}
