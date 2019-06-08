plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.9"
}

configure<org.gradle.guides.GuidesExtension> {
    repositoryPath.set("gradle-guides/building-cpp-executables")
    minimumGradleVersion.set("5.2.1")
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
