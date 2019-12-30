plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/creating-multi-project-builds")
    minimumGradleVersion.set("5.0")
    title.set("Creating Multi-project Builds")
    category.set("Getting Started")
}

tasks.named("test") {
    inputs.dir("samples/groovy-dsl")
    inputs.dir("samples/kotlin-dsl")
}
