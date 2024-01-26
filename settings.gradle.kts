plugins {
    id("com.gradle.enterprise").version("3.16.1")
    id("io.github.gradle.gradle-enterprise-conventions-plugin").version("0.7.6")
}

rootProject.name = "gradle-guides"

include("gradle-guides-plugin")
project(":gradle-guides-plugin").projectDir = file("subprojects/gradle-guides-plugin")
