plugins {
    `build-scan`
    id("org.gradle.guides.getting-started") version "0.15.10"
    id("org.gradle.guides.test-jvm-code") version "0.15.10"
}

guide {
    repositoryPath.set("gradle-guides/building-kotlin-jvm-libraries")
    minimumGradleVersion.set("5.0")
    title.set("Building Kotlin JVM Libraries")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
