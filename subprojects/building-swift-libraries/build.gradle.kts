plugins {
    id("org.gradle.guides.getting-started")
}

guide {
    repositoryPath.set("gradle-guides/building-swift-libraries")
    minimumGradleVersion.set("5.6.1")
}

// NOTE: Patch until we fix this in guide plugin
afterEvaluate {
    tasks.named("gitPublishReset") {
        enabled = true
    }
}
