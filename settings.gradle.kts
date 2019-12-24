plugins {
    id("com.gradle.enterprise").version("3.1.1")
}

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")

val guideAsCompositeBuilds = listOf(
    "creating-build-scans",
    "using-build-cache",
    "using-the-worker-api",
)
val guideAsProjectBuilds = listOf(
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
        "style-guide",
        "testing-gradle-plugins",
        "writing-getting-started-guides",
        "writing-gradle-plugins",
        "writing-gradle-tasks"
)

val misc = listOf("gradle-site-plugin", "greeting-plugin-example", "guides-test-fixtures")
val templates = listOf("gs-template", "topical-template", "tutorial-template")

(guideAsCompositeBuilds + misc + templates).forEach { includeBuild("subprojects/${it}") }
guideAsProjectBuilds.forEach {
    include(it)
    project(":${it}").projectDir = file("subprojects/${it}")
}

gradle.rootProject {
    val guideAsCompositeProjects by extra {
        guideAsCompositeBuilds
    }
    val guideAsProjects by extra {
        guideAsProjectBuilds
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