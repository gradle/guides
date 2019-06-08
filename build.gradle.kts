plugins {
    id("org.gradle.guides.getting-started") version "0.15.8"
    id("com.gradle.build-scan") version "2.3"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/@@GUIDE_SLUG@@"
    minimumGradleVersion.set("EDIT build.gradle.kts TO ADD MINIMUM GRADLE VERSION")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
