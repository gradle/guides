plugins {
    id("com.gradle.develocity").version("3.18.1")
    id("io.github.gradle.gradle-enterprise-conventions-plugin").version("0.10.2")
}

rootProject.name = "gradle-guides"

include("gradle-guides-plugin")
project(":gradle-guides-plugin").projectDir = file("subprojects/gradle-guides-plugin")
