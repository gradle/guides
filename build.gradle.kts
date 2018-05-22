plugins {
    id("com.gradle.build-scan") version "1.13.1"
    id("org.gradle.guides.getting-started") version "0.12.0"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/executing-gradle-builds-on-teamcity"
    mainAuthor = "Julia Alexandrova"
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
