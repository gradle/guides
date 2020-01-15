plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/migrating-from-maven")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Migrating from Maven to Gradle")
    category.set("Getting Started")
}

// TODO: Requires a maven seed sample
tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
