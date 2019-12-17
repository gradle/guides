package org.gradle.kotlin.dsl

import java.net.URL
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathConstants
import org.w3c.dom.Node
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.gradle.api.Project

fun Project.resolveLatestBuildScanPluginVersion() : String {
    val xml: String = URL("https://plugins.gradle.org/m2/com/gradle/build-scan/com.gradle.build-scan.gradle.plugin/maven-metadata.xml").readText()
    val doc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))
    val latestVersionNode = XPathFactory.newInstance().newXPath().evaluate("/metadata/versioning/latest", doc, XPathConstants.NODE) as Node
    return latestVersionNode.textContent
}
