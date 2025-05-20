package org.gradle.docs.internal

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class CheckLinksSpec extends Specification {
    @TempDir
    Path tempDir

    def "should extract absolute links from HTML"() {
        given:
        def html = """
            <html>
                <body>
                    <a href="https://example.com">Example</a>
                    <a href="https://gradle.org">Gradle</a>
                </body>
            </html>
        """

        when:
        def anchors = CheckLinks.getAnchors(html)

        then:
        anchors.size() == 2
        anchors.contains(new URI("https://example.com"))
        anchors.contains(new URI("https://gradle.org"))
    }

    def "should extract relative links from HTML"() {
        given:
        def html = """
            <html>
                <body>
                    <a href="/relative/path">Relative</a>
                    <a href="another/path">Another</a>
                </body>
            </html>
        """

        when:
        def anchors = CheckLinks.getAnchors(html)

        then:
        anchors.size() == 2
        anchors.contains(new URI("/relative/path"))
        anchors.contains(new URI("another/path"))
    }

    def "should handle empty document"() {
        given:
        def html = "<html><body></body></html>"

        when:
        def anchors = CheckLinks.getAnchors(html)

        then:
        anchors.isEmpty()
    }

    def "should handle malformed HTML"() {
        given:
        def html = """
            <html>
                <body>
                    <a href="https://example.com">Example
                    <a href="https://gradle.org">Gradle
                </body>
            </html>
        """

        when:
        def anchors = CheckLinks.getAnchors(html)

        then:
        anchors.size() == 2
        anchors.contains(new URI("https://example.com"))
        anchors.contains(new URI("https://gradle.org"))
    }

    private File createHtmlFile(String content) {
        def filePath = tempDir.resolve("test.html")
        Files.writeString(filePath, content)
        return filePath.toFile()
    }
} 
