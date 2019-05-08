plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.5"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-cpp-executables"
}

apply {
    from("gradle/cpp.gradle")
}

// NOTE: Patch until we fix this in guide plugin
afterEvaluate {
    tasks.named("gitPublishReset") {
        enabled = true
    }
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
