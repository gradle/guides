plugins {
    id("com.gradle.build-scan") version "2.0.2"
    id("org.gradle.guides.getting-started") version "0.15.5"
    id("org.ysb33r.vfs") version "1.0"
}

guide {
    repoPath = "gradle-guides/building-cpp-libraries"
}

apply {
    from("gradle/setup-googletest.gradle")
    from("gradle/cpp.gradle")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
