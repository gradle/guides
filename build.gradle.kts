plugins {
    id("com.gradle.build-scan") version "1.15.2"
    id("org.gradle.guides.getting-started") version "0.14.0"
    id("org.gradle.guides.test-jvm-code") version "0.14.0"
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    testImplementation("org.gradle:sample-check:0.4.2")
    testImplementation(gradleTestKit())
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-spring-boot-2-projects-with-gradle"
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
