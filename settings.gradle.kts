plugins {
    id("com.gradle.enterprise").version("3.4.1")
}

apply(from = "gradle/build-cache-configuration.settings.gradle.kts")

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")

val guides = listOf(
        // Move to corresponding places in user manual
        "using-build-cache",
        "performance",

        // Turn into a sample (or delete)
        "consuming-jvm-libraries",

        // Move to user manual (or delete if outdated)
        "migrating-build-logic-from-groovy-to-kotlin"
)

val misc = listOf("guides-test-fixtures")

misc.forEach { includeBuild("subprojects/${it}") }
guides.forEach {
    include(it)
    project(":${it}").projectDir = file("subprojects/${it}")

    if (it == "using-build-cache") {
        include("${it}:screenshots")
        project(":${it}:screenshots").projectDir = file("subprojects/${it}/screenshots")
    }
}

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
