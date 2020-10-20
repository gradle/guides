plugins {
    id("com.gradle.enterprise").version("3.4.1")
}

apply(from = "gradle/build-cache-configuration.settings.gradle.kts")

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")

val guides = listOf<String>()
val misc = listOf("guides-test-fixtures")

misc.forEach { includeBuild("subprojects/${it}") }
guides.forEach {
    include(it)
    project(":${it}").projectDir = file("subprojects/${it}")
}
include("using-build-cache:screenshots")
project(":using-build-cache:screenshots").projectDir = file("subprojects/using-build-cache/screenshots")

gradle.rootProject {
    val guideProjects by extra {
        guides
    }
}

gradleEnterprise {
    buildScan {
        server = "https://e.grdev.net"
        if (!System.getenv("CI").isNullOrEmpty()) {
            publishAlways()
            tag("CI")
        }
    }
}
