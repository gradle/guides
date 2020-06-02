plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/consuming-jvm-libraries")
    minimumGradleVersion.set("6.0")
    displayName.set("Consuming JVM Libraries")
    category.set("Getting Started")
}

tasks.named("checkAsciidoctorGuideContents") {
    enabled = false
}
