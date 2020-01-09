plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/building-swift-applications")
    minimumGradleVersion.set("5.6.1")
    category.set("Getting Started")
}

// TODO: Swift seems to be broken
tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
