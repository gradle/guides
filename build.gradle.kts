import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.7"
    id("org.gradle.guides.test-jvm-code") version "0.15.7"
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
    minimumGradleVersion.set("5.1.1")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
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
