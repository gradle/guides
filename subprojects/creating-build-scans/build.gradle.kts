import org.apache.tools.ant.filters.ReplaceTokens
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/creating-build-scans")
    minimumGradleVersion.set("5.1.1")
    category.set("Getting Started")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    docsTestImplementation("org.gradle:sample-check:0.12.1")
    docsTestImplementation(gradleTestKit())
}

fun resolveLatestBuildScanPluginVersion() : String {
    val xml: String = URL("https://plugins.gradle.org/m2/com/gradle/build-scan/com.gradle.build-scan.gradle.plugin/maven-metadata.xml").readText()
    val doc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))
    val latestVersionNode = XPathFactory.newInstance().newXPath().evaluate("/metadata/versioning/latest", doc, XPathConstants.NODE) as Node
    return latestVersionNode.textContent
}

tasks {
    val preProcessSamples by registering(Copy::class) {
        into("$buildDir/samples")
        from("samples")
        val tokens = mapOf("scanPluginVersion" to resolveLatestBuildScanPluginVersion())
        filter<ReplaceTokens>("tokens" to tokens)
    }

    guidesMultiPage {
        dependsOn(preProcessSamples)
        attributes.putAll(mapOf(
                "samplescodedir" to project.file("build/samples/code").absolutePath
        ))
    }

    docsTest {
        dependsOn(preProcessSamples)
        systemProperty("samplesDir", "$buildDir/samples")
    }
}

// TODO: Need code edit to fully test the project