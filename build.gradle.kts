plugins {
    id("org.gradle.guides.tutorial") version "0.13.2"
    id("com.gradle.build-scan") version "1.13.4"

    // Uncomment this line if you need test JVM code snippets
    // id("org.gradle.guides.test-jvm-code") version "0.13.2"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/@@GUIDE_SLUG@@"
    mainAuthor = "EDIT build.gradle TO ADD AUTHOR"
}

buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
