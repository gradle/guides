plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/using-the-worker-api")
    minimumGradleVersion.set("5.6")
    displayName.set("Using the Worker API")
    category.set("Getting Started")
}

// TODO: Need seed sample
tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
