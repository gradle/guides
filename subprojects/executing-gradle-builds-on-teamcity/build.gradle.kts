plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/executing-gradle-builds-on-teamcity")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Executing Gradle builds on TeamCity")
    category.set("Getting Started")
}

// TODO: background command required
// TODO: gradle-site-plugin required
tasks.named("checkAsciidoctorContents") {
    enabled = false
}
