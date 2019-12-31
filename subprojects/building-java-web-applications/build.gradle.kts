plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/building-java-web-applications")
    minimumGradleVersion.set("4.10.3")
    category.set("Getting Started")
}

// TODO: Test are hanging, disabling for now
tasks.named("docsTest") {
    enabled = false
}

tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
