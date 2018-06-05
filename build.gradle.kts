plugins {
    groovy
    id("com.gradle.build-scan") version "1.13.4"
    id("org.gradle.guides.getting-started") version "0.13.3"
    id("org.gradle.guides.test-jvm-code") version "0.13.3"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/building-java-applications"
    mainAuthor = "Ken Kousen"
}

dependencies {
    testCompile(localGroovy())
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
