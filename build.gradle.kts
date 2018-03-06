plugins {
    id("com.gradle.build-scan") version "1.12.1"
    id("org.gradle.guides.getting-started") version "0.11.2"
    id("org.gradle.guides.test-jvm-code") version "0.11.2"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-java-applications"
    mainAuthor = "Ken Kousen"
}
buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
