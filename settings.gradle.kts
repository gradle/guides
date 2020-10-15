plugins {
    id("com.gradle.enterprise").version("3.1.1")
}

apply(from = "gradle/build-cache-configuration.settings.gradle.kts")

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")

val guides = listOf(
        "building-java-web-applications",
        "building-spring-boot-2-projects-with-gradle",
        "consuming-jvm-libraries",
        "creating-build-scans",
        "creating-new-gradle-builds",
        "designing-gradle-plugins",
        "executing-gradle-builds-on-jenkins",
        "executing-gradle-builds-on-teamcity",
        "executing-gradle-builds-on-travisci",
        "implementing-gradle-plugins",
        "migrating-build-logic-from-groovy-to-kotlin",
        "performance",
        "publishing-plugins-to-gradle-plugin-portal",
        "running-webpack-with-gradle",
        "testing-gradle-plugins",
        "using-build-cache",
        "using-the-worker-api"
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
