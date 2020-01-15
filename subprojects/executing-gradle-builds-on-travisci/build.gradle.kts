plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/executing-gradle-builds-on-travisci")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Executing Gradle builds on Travis CI")
    category.set("Getting Started")
}

// TODO: requires gradle-site-plugins
tasks.named("checkAsciidoctorGuideContents") {
    enabled = false
}
