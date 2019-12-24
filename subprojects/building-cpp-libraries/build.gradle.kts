import org.gradle.guides.GenerateReadMeFile

plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/building-cpp-libraries")
    minimumGradleVersion.set("5.5.1")
    title.set("Building C++ libraries")
    category.set("Getting Started")
}

// NOTE: Patch until we fix this in guide plugin
afterEvaluate {
    tasks.named("gitPublishReset") {
        enabled = true
    }
}
