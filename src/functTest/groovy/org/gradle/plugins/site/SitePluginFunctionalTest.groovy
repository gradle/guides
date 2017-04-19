package org.gradle.plugins.site

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import spock.lang.Unroll

import static org.gradle.plugins.site.SitePlugin.SITE_TASK_NAME

class SitePluginFunctionalTest extends AbstractFunctionalTest {

    def "can generate site for default conventions"() {
        when:
        build(SITE_TASK_NAME)

        then:
        def outputDir = new File(projectDir, 'build/docs/site')
        assertSiteFiles(outputDir)
        def doc = parseIndexHtml(outputDir)
        findWebsiteLinkDivs(doc).empty
        findCodeLinkDivs(doc).empty
        findJavaSourceCompatibilityDiv(doc).empty
        findJavaTargetCompatibilityDiv(doc).empty
    }

    def "can generate site for custom conventions"() {
        given:
        def customOutputDir = 'build/custom/site'
        def websiteUrl = 'https://gradle.org'
        def vcsUrl = 'https://github.com/gradle/gradle'
        buildFile << """
            site {
                outputDir = file('$customOutputDir')
                websiteUrl = '$websiteUrl'
                vcsUrl = '$vcsUrl'
            }
        """

        when:
        build(SITE_TASK_NAME)

        then:
        File outputDir = new File(projectDir, customOutputDir)
        assertSiteFiles(outputDir)
        def doc = parseIndexHtml(outputDir)
        def foundWebSiteDivs = findWebsiteLinkDivs(doc)
        foundWebSiteDivs.size() == 1
        def webSiteLinks = findAhrefs(foundWebSiteDivs.first(), websiteUrl)
        webSiteLinks.size() == 1
        def foundVcsDivs = findCodeLinkDivs(doc)
        foundVcsDivs.size() == 1
        def vcsUrlLinks = findAhrefs(foundVcsDivs.first(), vcsUrl)
        vcsUrlLinks.size() == 1
    }

    @Unroll
    def "can derive and render Java-specific information for #plugin plugin"() {
        given:
        def sourceCompatibility = '1.6'
        def targetCompatibility = '1.7'
        buildFile << """
            apply plugin: '$plugin'
            
            sourceCompatibility = '$sourceCompatibility'
            targetCompatibility = '$targetCompatibility'
        """

        when:
        build(SITE_TASK_NAME)

        then:
        def outputDir = new File(projectDir, 'build/docs/site')
        assertSiteFiles(outputDir)
        def doc = parseIndexHtml(outputDir)
        def javaSourceCompatibilityDivs = findJavaSourceCompatibilityDiv(doc)
        javaSourceCompatibilityDivs.size() == 1
        javaSourceCompatibilityDivs.first().text() == sourceCompatibility
        def javaTargetCompatibilityDivs = findJavaTargetCompatibilityDiv(doc)
        javaTargetCompatibilityDivs.size() == 1
        javaTargetCompatibilityDivs.first().text() == targetCompatibility

        where:
        plugin << ['java', 'groovy']
    }

    static void assertSiteFiles(File directory) {
        assert new File(directory, 'index.html').isFile()
        assert new File(directory, 'css/bootstrap.css').isFile()
        assert new File(directory, 'css/bootstrap-responsive.css').isFile()
        assert new File(directory, 'js/bootstrap.js').isFile()
        assert new File(directory, 'img/elephant-corner.png').isFile()
    }

    static Document parseIndexHtml(File outputDir) {
        Jsoup.parse(new File(outputDir, 'index.html'), 'UTF-8')
    }

    static Elements findWebsiteLinkDivs(Document doc) {
        findDivsById(doc, 'website-link')
    }

    static Elements findCodeLinkDivs(Document doc) {
        findDivsById(doc, 'code-link')
    }

    static Elements findJavaSourceCompatibilityDiv(Document doc) {
        findDivsById(doc, 'java-source-compatibility')
    }

    static Elements findJavaTargetCompatibilityDiv(Document doc) {
        findDivsById(doc, 'java-target-compatibility')
    }

    static Elements findDivsById(Document doc, String id) {
        doc.select("div[id=$id]")
    }

    static Elements findAhrefs(Element element, String url) {
        element.select("a[href=$url")
    }
}
