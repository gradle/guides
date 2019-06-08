plugins {
    `build-scan`
    id("org.gradle.guides.getting-started") version "0.15.8"
    id("org.gradle.guides.test-jvm-code") version "0.15.8"
}

configure<org.gradle.guides.GuidesExtension> {
    repositoryPath.set("gradle-guides/building-kotlin-jvm-libraries")
    minimumGradleVersion.set("5.0")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
