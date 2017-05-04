package org.gradle.plugins.site

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import spock.lang.Unroll

import static org.gradle.plugins.site.SitePlugin.SITE_TASK_NAME

class SitePluginFunctionalTest extends AbstractFunctionalTest {

    def "provides site task"() {
        when:
        def buildResult = build('tasks', '--all')

        then:
        buildResult.output.contains("""Documentation tasks
-------------------
site - Generates a web page containing information about the project.""")
    }

    def "can generate site for default conventions"() {
        when:
        build(SITE_TASK_NAME)

        then:
        def outputDir = new File(projectDir, 'build/docs/site')
        assertSiteFiles(outputDir)
        def doc = parseIndexHtml(outputDir)
        findWebsiteLinkAs(doc).empty
        findCodeLinkAs(doc).empty
        findJavaSourceCompatibilityTds(doc).empty
        findJavaTargetCompatibilityTds(doc).empty
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
        def foundWebSiteLinks = findWebsiteLinkAs(doc)
        foundWebSiteLinks.size() == 1
        def webSiteLinks = findAhrefs(foundWebSiteLinks.first(), websiteUrl)
        webSiteLinks.size() == 1
        def foundVcsLis = findCodeLinkAs(doc)
        foundVcsLis.size() == 1
        def vcsUrlLinks = findAhrefs(foundVcsLis.first(), vcsUrl)
        vcsUrlLinks.size() == 1
    }

    def "can query extension properties"() {
        when:
        buildFile << """
            task assertDefaultExtensionProperties {
                doLast {
                    assert project.extensions.site.outputDir == file("\$buildDir/docs/site")
                    assert !project.extensions.site.websiteUrl
                    assert !project.extensions.site.vcsUrl
                }
            }
        """
        build('assertDefaultExtensionProperties')

        then:
        noExceptionThrown()

        when:
        def customOutputDir = 'build/custom/site'
        def websiteUrl = 'https://gradle.org'
        def vcsUrl = 'https://github.com/gradle/gradle'
        buildFile << """
            site {
                outputDir = file('$customOutputDir')
                websiteUrl = '$websiteUrl'
                vcsUrl = '$vcsUrl'
            }
            
            task assertCustomExtensionProperties {
                doLast {
                    assert project.extensions.site.outputDir == file('$customOutputDir')
                    assert project.extensions.site.websiteUrl == '$websiteUrl'
                    assert project.extensions.site.vcsUrl == '$vcsUrl'
                }
            }
        """
        build('assertCustomExtensionProperties')

        then:
        noExceptionThrown()
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
        def javaSourceCompatibilityDivs = findJavaSourceCompatibilityTds(doc)
        javaSourceCompatibilityDivs.size() == 1
        javaSourceCompatibilityDivs.first().text() == sourceCompatibility
        def javaTargetCompatibilityDivs = findJavaTargetCompatibilityTds(doc)
        javaTargetCompatibilityDivs.size() == 1
        javaTargetCompatibilityDivs.first().text() == targetCompatibility

        where:
        plugin << ['java', 'groovy']
    }

    static void assertSiteFiles(File directory) {
        assert new File(directory, 'index.html').isFile()
        assert new File(directory, 'css/bootstrap.css').isFile()
        assert new File(directory, 'css/bootstrap-responsive.css').isFile()
        assert new File(directory, 'img/elephant-corner.png').isFile()
    }

    static Document parseIndexHtml(File outputDir) {
        Jsoup.parse(new File(outputDir, 'index.html'), 'UTF-8')
    }

    static Elements findWebsiteLinkAs(Document doc) {
        findAsById(doc, 'website-link')
    }

    static Elements findCodeLinkAs(Document doc) {
        findAsById(doc, 'code-link')
    }

    static Elements findJavaSourceCompatibilityTds(Document doc) {
        findTdsById(doc, 'java-source-compatibility')
    }

    static Elements findJavaTargetCompatibilityTds(Document doc) {
        findTdsById(doc, 'java-target-compatibility')
    }

    static Elements findAsById(Document doc, String id) {
        doc.select("a[id=$id]")
    }

    static Elements findTdsById(Document doc, String id) {
        doc.select("td[id=$id]")
    }

    static Elements findAhrefs(Element element, String url) {
        element.select("a[href=$url")
    }
}
