plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.7"
    id("org.gradle.guides.test-jvm-code") version "0.15.7"
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    constraints {
        testImplementation("org.codehaus.groovy:groovy-all:2.5.6")
    }
    testImplementation("org.gradle:sample-check:0.5.0")
    testImplementation(gradleTestKit())
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-spring-boot-2-projects-with-gradle"
    minimumGradleVersion.set("4.10.3")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
