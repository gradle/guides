plugins {
    id("com.gradle.develocity").version("4.3.2")
    id("com.autonomousapps.build-health").version("3.5.1")
    id("io.github.gradle.develocity-conventions-plugin").version("0.13.0")
}

rootProject.name = "gradle-guides"

include("gradle-guides-plugin")
