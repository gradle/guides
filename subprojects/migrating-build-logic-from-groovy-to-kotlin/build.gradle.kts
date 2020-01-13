plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/migrating-build-logic-from-groovy-to-kotlin")
    minimumGradleVersion.set("5.0")
    displayName.set("Migrating build logic from Groovy to Kotlin")
    category.set("Topical")
}

tasks.docsTest {
    inputs.property("androidHome", System.getenv("ANDROID_HOME") ?: "")
}

repositories {
    maven(url = "https://repo.gradle.org/gradle/libs")
}

dependencies {
    constraints {
        docsTestImplementation("org.codehaus.groovy:groovy-all:2.5.4")
    }
    docsTestImplementation("org.gradle:sample-check:0.12.2")
    docsTestImplementation(gradleTestKit())
}

// TODO: Requires more investiguating on how to test this project
tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
