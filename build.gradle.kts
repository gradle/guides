plugins {
    id("com.gradle.build-scan") version "2.3"
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}

//gradle.includedBuilds.stream().map { it.task("pullRequest") }