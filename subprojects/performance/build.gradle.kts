plugins {
    id("org.gradle.guides.topical") version "0.15.13"
    id("org.gradle.guides.test-jvm-code") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/performance")
    minimumGradleVersion.set("4.10.3")
    title.set("Improving the Performance of Gradle Builds")
}
