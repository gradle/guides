plugins {
    id("com.gradle.enterprise").version("3.1.1")
}

includeBuild("subprojects/gradle-guides-plugin")

val guides = listOf(
    "subprojects/building-cpp-applications",
    "subprojects/building-cpp-libraries",
    "subprojects/building-groovy-libraries",
    "subprojects/building-java-9-modules",
    "subprojects/building-java-applications",
    "subprojects/building-java-libraries",
    "subprojects/building-java-web-applications",
    "subprojects/building-kotlin-jvm-libraries",
    "subprojects/building-scala-libraries",
    "subprojects/building-swift-applications",
    "subprojects/building-swift-libraries",
    "subprojects/building-spring-boot-2-projects-with-gradle",
    "subprojects/creating-build-scans",
    "subprojects/creating-multi-project-builds",
    "subprojects/creating-new-gradle-builds",
    "subprojects/consuming-jvm-libraries",
    "subprojects/designing-gradle-plugins",
    "subprojects/executing-gradle-builds-on-jenkins",
    "subprojects/executing-gradle-builds-on-teamcity",
    "subprojects/executing-gradle-builds-on-travisci",
    "subprojects/implementing-gradle-plugins",
    "subprojects/using-build-cache",
    "subprojects/using-the-worker-api",
    "subprojects/writing-getting-started-guides",
    "subprojects/writing-gradle-plugins",
    "subprojects/writing-gradle-tasks",
    "subprojects/migrating-from-maven",
    "subprojects/migrating-build-logic-from-groovy-to-kotlin",
    "subprojects/performance",
    "subprojects/publishing-plugins-to-gradle-plugin-portal",
    "subprojects/running-webpack-with-gradle",
    "subprojects/style-guide",
    "subprojects/testing-gradle-plugins"
)

val misc = listOf("subprojects/gradle-site-plugin", "subprojects/greeting-plugin-example", "subprojects/guides-test-fixtures")
val templates = listOf("subprojects/gs-template", "subprojects/topical-template", "subprojects/tutorial-template")

(guides + misc + templates).forEach { includeBuild(it) }

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