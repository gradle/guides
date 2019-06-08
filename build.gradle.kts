plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.topical") version "0.15.9"
    id("org.gradle.guides.test-jvm-code") version "0.15.9"
}

configure<org.gradle.guides.GuidesExtension> {
    repositoryPath.set("gradle-guides/performance")
    minimumGradleVersion.set("4.10.3")
    title.set("Improving the Performance of Gradle Builds")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
