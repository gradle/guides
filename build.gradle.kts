plugins {
    id("com.gradle.build-scan") version "1.15.2"
    id("org.gradle.guides.topical") version "0.13.2"
    id("org.gradle.guides.test-jvm-code") version "0.13.2"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/migrating-build-logic-from-groovy-to-kotlin"
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}

tasks.getByName<Test>("test") {
    inputs.property("androidHome", System.getenv("ANDROID_HOME"))
}
