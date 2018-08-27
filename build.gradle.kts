plugins {
    id("com.gradle.build-scan") version "1.15.2"
    id("org.gradle.guides.topical") version "0.14.1"
    id("org.gradle.guides.test-jvm-code") version "0.14.1"
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

repositories {
    maven(url = "https://repo.gradle.org/gradle/libs")
}

dependencies {
    constraints {
        "testImplementation"("org.codehaus.groovy:groovy-all:2.4.15")
    }
    testImplementation("org.gradle:sample-check:0.5.0")
    testImplementation(gradleTestKit())
}
