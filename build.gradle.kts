plugins {
    `build-scan`
    id("org.gradle.guides.topical") version "0.15.1"
    id("org.gradle.guides.test-jvm-code") version "0.15.1"
}

guide {
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

tasks.test {
    inputs.property("androidHome", System.getenv("ANDROID_HOME") ?: "")
}

repositories {
    maven(url = "https://repo.gradle.org/gradle/libs")
}

dependencies {
    constraints {
        testImplementation("org.codehaus.groovy:groovy-all:2.5.4")
    }
    testImplementation("org.gradle:sample-check:0.6.0")
    testImplementation(gradleTestKit())
}
