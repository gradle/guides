plugins {
    `build-scan`
    id("org.gradle.guides.getting-started") version "0.15.1"
    id("org.gradle.guides.test-jvm-code") version "0.15.1"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-kotlin-jvm-libraries"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
