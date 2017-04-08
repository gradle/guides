package org.gradle.plugins.site

import static org.gradle.plugins.site.SitePlugin.SITE_TASK_NAME

class SitePluginFunctionalTest extends AbstractFunctionalTest {

    def "can generate site for default conventions"() {
        when:
        build(SITE_TASK_NAME)

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
        build(SITE_TASK_NAME)

        then:
        def indexFile = new File(projectDir, "$customOutputDir/index.html")
        indexFile.isFile()
        indexFile.text == projectDir.name
    }
}
