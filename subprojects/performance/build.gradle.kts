plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/performance")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Improving the Performance of Gradle Builds")
    category.set("Topical")
}

// TODO: requires some investigation for testing
tasks.named("checkAsciidoctorGuideContents") {
    enabled = false
}
