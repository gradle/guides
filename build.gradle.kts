plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.14.0"
    id("org.gradle.guides.test-jvm-code") version "0.14.0"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/migrating-from-maven"
    mainAuthor = "Peter Ledbrook"
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
