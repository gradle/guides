plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/creating-new-gradle-builds")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Creating new Gradle builds")
    category.set("Getting Started")
}

tasks.named("checkAsciidoctorGuideContents") {
    enabled = false
}
