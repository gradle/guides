plugins {
    id("com.gradle.enterprise").version("3.6.3")
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin").version("0.7.2")
}

apply(from = "gradle/build-cache-configuration.settings.gradle.kts")

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")
includeBuild("subprojects/guides-test-fixtures")
include("using-build-cache:screenshots")
project(":using-build-cache:screenshots").projectDir = file("subprojects/using-build-cache/screenshots")
