plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.topical") version "0.15.9"
}

configure<org.gradle.guides.GuidesExtension> {
    repositoryPath.set("gradle-guides/style-guide")
    minimumGradleVersion.set("4.10.3")
    title.set("Style Guide for Gradle Guide Authors")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
