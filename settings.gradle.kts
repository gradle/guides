plugins {
    id("com.gradle.develocity").version("4.3.2")
    id("com.autonomousapps.build-health").version("3.6.1")
    id("io.github.gradle.develocity-conventions-plugin").version("0.14.1")
}

rootProject.name = "gradle-guides"

include("gradle-guides-plugin")
