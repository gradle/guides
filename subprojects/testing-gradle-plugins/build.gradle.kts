plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/testing-gradle-plugins")
    minimumGradleVersion.set("4.10.3")
    category.set("Getting Started")
}

// TODO: Need seed sample or something similar
tasks.named("checkAsciidoctorContents") {
    enabled = false
}
