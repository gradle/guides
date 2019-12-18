plugins {
    id("org.gradle.guides.getting-started") version "0.15.13"
    id("org.gradle.guides.test-jvm-code") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/building-kotlin-jvm-libraries")
    minimumGradleVersion.set("5.0")
    title.set("Building Kotlin JVM Libraries")
}
