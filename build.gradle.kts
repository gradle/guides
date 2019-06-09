plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.12"
}

guide {
    repositoryPath.set("gradle-guides/writing-getting-started-guides")
    minimumGradleVersion.set("4.10.3")
    title.set("Writing Gradle Guides")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
