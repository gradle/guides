import org.apache.tools.ant.filters.*
import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("com.gradle.build-scan") version "1.16"
    id("org.gradle.guides.getting-started") version "0.14.0"
    id("org.gradle.guides.test-jvm-code") version "0.14.0"
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

guide {
    repoPath = "gradle-guides/creating-build-scans"
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
    val preProcessSamples by creating(Copy::class) {
        into("$buildDir/samples")
        from("samples")
        dependsOn("configurePreProcessSamples")
    }

    val configurePreProcessSamples by creating {
        doLast {
            val tokens = mapOf("scanPluginVersion" to resolveLatestBuildScanPluginVersion())
            preProcessSamples.inputs.properties(tokens)
            preProcessSamples.filter<ReplaceTokens>("tokens" to tokens)
        }
    }

    val asciidoctor by getting(AsciidoctorTask::class) {
        dependsOn(preProcessSamples)
        attributes.putAll(mapOf(
            "samplescodedir" to project.file("build/samples/code").absolutePath
        ))
    }

    val test by getting(Test::class) {
        dependsOn(preProcessSamples)
        systemProperty("samplesDir", "$buildDir/samples")
    }
}
