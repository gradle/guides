package org.gradle.plugins.site

class SitePluginFunctionalTest extends AbstractFunctionalTest {

    def "can generate site"() {
        when:
        build('site')

        then:
        def indexFile = new File(projectDir,'build/docs/site/index.html')
        indexFile.isFile()
        indexFile.text == projectDir.name
    }
}
