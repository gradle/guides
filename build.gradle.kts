import org.gradle.guides.GenerateReadMeFile

plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.12"
}

guide {
    repositoryPath.set("gradle-guides/building-cpp-applications")
    minimumGradleVersion.set("5.2.1")
    title.set("Building C++ Executables")
}

tasks.withType(GenerateReadMeFile::class).configureEach {
    title.set(guide.title.map { it.replace("C++", "{cpp}") })
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