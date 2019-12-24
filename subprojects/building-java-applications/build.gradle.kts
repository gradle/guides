plugins {
    groovy
    id("org.gradle.guides")
    id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/building-java-applications")
    minimumGradleVersion.set("5.4.1")
    category.set("Getting Started")
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
