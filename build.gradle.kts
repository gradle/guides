plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.7"
}

guide {
    repoPath = "gradle-guides/building-cpp-libraries"
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
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
