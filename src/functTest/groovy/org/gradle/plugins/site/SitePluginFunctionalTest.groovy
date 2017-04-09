package org.gradle.plugins.site

import static org.gradle.plugins.site.SitePlugin.SITE_TASK_NAME

class SitePluginFunctionalTest extends AbstractFunctionalTest {

    def "can generate site for default conventions"() {
        when:
        build(SITE_TASK_NAME)

        then:
        assertSiteFiles(new File(projectDir, 'build/docs/site'))
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
        assertSiteFiles(new File(projectDir, customOutputDir))
    }

    static void assertSiteFiles(File directory) {
        assert new File(directory, "index.html").isFile()
        assert new File(directory, "css/bootstrap.css").isFile()
        assert new File(directory, "css/bootstrap-responsive.css").isFile()
        assert new File(directory, "js/bootstrap.js").isFile()
        assert new File(directory, "img/elephant-corner.png").isFile()
    }
}
