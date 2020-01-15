plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/writing-gradle-tasks")
    minimumGradleVersion.set("5.0")
    category.set("Getting Started")
}

// TODO: Need seed sample
tasks.named("checkAsciidoctorGuideContents") {
    enabled = false
}
