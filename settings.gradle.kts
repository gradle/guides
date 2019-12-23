plugins {
    id("com.gradle.enterprise").version("3.1.1")
}

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
    "building-swift-applications",
    "building-swift-libraries",
    "building-spring-boot-2-projects-with-gradle",
    "creating-build-scans",
    "creating-multi-project-builds",
    "creating-new-gradle-builds",
    "consuming-jvm-libraries",
    "designing-gradle-plugins",
    "executing-gradle-builds-on-jenkins",
    "executing-gradle-builds-on-teamcity",
    "executing-gradle-builds-on-travisci",
    "implementing-gradle-plugins",
    "using-build-cache",
    "using-the-worker-api",
    "writing-getting-started-guides",
    "writing-gradle-plugins",
    "writing-gradle-tasks",
    "migrating-from-maven",
    "migrating-build-logic-from-groovy-to-kotlin",
    "performance",
    "publishing-plugins-to-gradle-plugin-portal",
    "running-webpack-with-gradle",
    "style-guide",
    "testing-gradle-plugins"
)

val misc = listOf("gradle-site-plugin", "greeting-plugin-example", "guides-test-fixtures")
val templates = listOf("gs-template", "topical-template", "tutorial-template")

(guides + misc + templates).forEach { includeBuild("subprojects/${it}") }

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