plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.topical") version "0.15.8"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/style-guide"
    minimumGradleVersion.set("4.10.3")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
