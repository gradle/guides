plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/creating-multi-project-builds")
    minimumGradleVersion.set("5.0")
    displayName.set("Creating Multi-project Builds")
    category.set("Getting Started")
}

tasks.named("docsTest") {
    inputs.dir("samples/groovy-dsl")
    inputs.dir("samples/kotlin-dsl")
}

// TODO: Add code to folders