plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/consuming-jvm-libraries")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Consuming JVM Libraries")
    category.set("Getting Started")
}

tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
