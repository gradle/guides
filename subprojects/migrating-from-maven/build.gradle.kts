plugins {
    id("org.gradle.guides.getting-started") version "0.15.13"
    id("org.gradle.guides.test-jvm-code") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/migrating-from-maven")
    minimumGradleVersion.set("4.10.3")
    title.set("Migrating from Maven to Gradle")
}
