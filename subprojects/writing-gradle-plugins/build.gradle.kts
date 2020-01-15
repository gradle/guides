plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/writing-gradle-plugins")
    minimumGradleVersion.set("4.10.3")
    category.set("Getting Started")
}

tasks.named("checkAsciidoctorContents") {
    enabled = false
}
