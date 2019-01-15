plugins {
    id("com.gradle.build-scan") version "2.0.2"
    id("org.gradle.guides.getting-started") version "0.15.0"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-cpp-executables"
    mainAuthor = "Schalk Cronjé"
}

apply {
    from("gradle/cpp.gradle")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
