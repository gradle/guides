plugins {
    id("com.gradle.build-scan") version "1.13.4"
    id("org.gradle.guides.getting-started") version "0.13.2"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/writing-getting-started-guides"
}

buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
