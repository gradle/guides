package org.gradle.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import java.io.File
import org.w3c.dom.*
import javax.xml.parsers.*
import org.xml.sax.*
import java.net.*
import javax.xml.xpath.*

fun Project.resolveLatestBuildScanPluginVersion() : String {
    val xml: String = URL("https://plugins.gradle.org/m2/com/gradle/build-scan/com.gradle.build-scan.gradle.plugin/maven-metadata.xml").readText()
    val doc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(java.io.StringReader(xml)))
    val latestVersionNode = XPathFactory.newInstance().newXPath().evaluate("/metadata/versioning/latest", doc, XPathConstants.NODE) as Node
    return latestVersionNode.textContent
}
