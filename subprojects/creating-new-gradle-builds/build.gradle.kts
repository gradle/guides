plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/creating-new-gradle-builds")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Creating new Gradle builds")
    category.set("Getting Started")
}

tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
