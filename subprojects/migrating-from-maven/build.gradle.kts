plugins {
    id("org.gradle.guides")
    id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/migrating-from-maven")
    minimumGradleVersion.set("4.10.3")
    title.set("Migrating from Maven to Gradle")
    category.set("Getting Started")
}
