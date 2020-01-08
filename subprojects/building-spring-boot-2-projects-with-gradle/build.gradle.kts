plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/building-spring-boot-2-projects-with-gradle")
    minimumGradleVersion.set("4.10.3")
    displayName.set("Building Spring Boot 2 Applications with Gradle")
    category.set("Getting Started")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    constraints {
        docsTestImplementation("org.codehaus.groovy:groovy-all:2.5.6")
    }
    docsTestImplementation("org.gradle:sample-check:0.11.0")
    docsTestImplementation(gradleTestKit())
}

tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
