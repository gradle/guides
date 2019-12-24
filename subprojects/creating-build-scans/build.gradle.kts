import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("org.gradle.guides.getting-started")
    id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/creating-build-scans")
    minimumGradleVersion.set("5.1.1")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    testImplementation("org.gradle:sample-check:0.7.0")
    testImplementation(gradleTestKit())
}

fun resolveLatestBuildScanPluginVersion() : String {
    val xml: String = java.net.URL("https://plugins.gradle.org/m2/com/gradle/build-scan/com.gradle.build-scan.gradle.plugin/maven-metadata.xml").readText()
    val doc: org.w3c.dom.Document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(org.xml.sax.InputSource(java.io.StringReader(xml)))
    val latestVersionNode = javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate("/metadata/versioning/latest", doc, javax.xml.xpath.XPathConstants.NODE) as org.w3c.dom.Node
    return latestVersionNode.textContent
}

tasks {
    val preProcessSamples by registering(Copy::class) {
        into("$buildDir/samples")
        from("samples")
        val tokens = mapOf("scanPluginVersion" to resolveLatestBuildScanPluginVersion())
        filter<ReplaceTokens>("tokens" to tokens)
    }

    asciidoctor {
        dependsOn(preProcessSamples)
        attributes.putAll(mapOf(
            "samplescodedir" to project.file("build/samples/code").absolutePath
        ))
    }

    test {
        dependsOn(preProcessSamples)
        systemProperty("samplesDir", "$buildDir/samples")
    }
}
