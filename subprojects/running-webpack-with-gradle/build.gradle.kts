plugins {
    id("org.gradle.guides.getting-started") version "0.15.13"
    id("org.gradle.guides.test-jvm-code") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/running-webpack-with-gradle")
    minimumGradleVersion.set("4.10.3")
    title.set("Running Webpack with Gradle")
}
