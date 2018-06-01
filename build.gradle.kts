plugins {
    id("com.gradle.build-scan") version "1.13.4"
    id("org.gradle.guides.getting-started") version "0.13.1"
    id("org.gradle.guides.test-jvm-code") version "0.13.1"
}

configure<org.gradle.guides.GuidesExtension> {
    setRepoPath("gradle-guides/creating-build-scans")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
