plugins {
    id("com.gradle.enterprise").version("3.1.1")
}

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")

val guides = listOf(
        "building-cpp-applications",
        "building-cpp-libraries",
        "building-groovy-libraries",
        "building-java-9-modules",
        "building-java-applications",
        "building-java-libraries",
        "building-java-web-applications",
        "building-kotlin-jvm-libraries",
        "building-scala-libraries",
        "building-spring-boot-2-projects-with-gradle",
        "building-swift-applications",
        "building-swift-libraries",
        "consuming-jvm-libraries",
        "creating-build-scans",
        "creating-multi-project-builds",
        "creating-new-gradle-builds",
        "designing-gradle-plugins",
        "executing-gradle-builds-on-jenkins",
        "executing-gradle-builds-on-teamcity",
        "executing-gradle-builds-on-travisci",
        "implementing-gradle-plugins",
        "migrating-build-logic-from-groovy-to-kotlin",
        "migrating-from-maven",
        "performance",
        "publishing-plugins-to-gradle-plugin-portal",
        "running-webpack-with-gradle",
        "testing-gradle-plugins",
        "using-build-cache",
        "using-the-worker-api",
        "writing-getting-started-guides",
        "writing-gradle-plugins",
        "writing-gradle-tasks"
)

val misc = listOf("gradle-site-plugin", "greeting-plugin-example", "guides-test-fixtures")

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
        setTermsOfServiceUrl("https://gradle.com/terms-of-service")
        setTermsOfServiceAgree("yes")
        if (!System.getenv("CI").isNullOrEmpty()) {
            publishAlways()
            tag("CI")
        }
    }
}