import org.apache.tools.ant.filters.*
import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("com.gradle.build-scan") version "1.14"
    id("org.gradle.guides.getting-started") version "0.13.3"
    id("org.gradle.guides.test-jvm-code") version "0.13.3"
}

configure<org.gradle.guides.GuidesExtension> {
    setRepoPath("gradle-guides/creating-build-scans")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}



tasks {
    "preProcessSamples"(Copy::class) {
        val tokens = mapOf("scanPluginVersion" to resolveLatestBuildScanPluginVersion())
        inputs.properties(tokens)

        from("samples") {
            filter<ReplaceTokens>("tokens" to tokens)
        }

        into("$buildDir/samples")
    }
    val asciidoctor by getting(AsciidoctorTask::class) {
        dependsOn("preProcessSamples")
        attributes.putAll(mapOf(
            "samplescodedir" to project.file("build/samples/code").absolutePath
        ))
    }
    val test by getting(Test::class) {
        dependsOn("preProcessSamples")
        systemProperty("samplesDir", "$buildDir/samples")
    }
}
