plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/building-java-libraries")
    minimumGradleVersion.set("5.0")
    category.set("Getting Started")
}

tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
