plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.9"
}

configure<org.gradle.guides.GuidesExtension> {
    repositoryPath.set("gradle-guides/executing-gradle-builds-on-travisci")
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
