plugins {
    id("com.gradle.build-scan") version "1.12.1"
    id("org.gradle.guides.getting-started") version "0.11.5"
    id("org.gradle.guides.test-jvm-code") version "0.11.5"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/migrating-from-maven"
    mainAuthor = "Peter Ledbrook"
}

buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
