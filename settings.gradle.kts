plugins {
    id("com.gradle.develocity").version("4.3.1")
    id("com.autonomousapps.build-health").version("2.17.0")
    id("io.github.gradle.gradle-enterprise-conventions-plugin").version("0.10.2")
}

rootProject.name = "gradle-guides"

include("gradle-guides-plugin")
