plugins {
    groovy
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.getting-started") version "0.15.13"
    id("org.gradle.guides.test-jvm-code") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/building-java-applications")
    minimumGradleVersion.set("5.4.1")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    constraints {
        testImplementation("org.codehaus.groovy:groovy-all:2.5.3")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
