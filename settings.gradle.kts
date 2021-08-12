plugins {
    id("com.gradle.enterprise").version("3.6.3")
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin").version("0.7.2")
}

rootProject.name = "gradle-guides"

includeBuild("subprojects/gradle-guides-plugin")
include("using-build-cache:screenshots")
project(":using-build-cache:screenshots").projectDir = file("subprojects/using-build-cache/screenshots")
