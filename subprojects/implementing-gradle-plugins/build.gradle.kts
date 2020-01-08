import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/implementing-gradle-plugins")
    minimumGradleVersion.set("5.0")
    category.set("Topical")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    docsTestImplementation("org.gradle:sample-check:0.11.1")
    docsTestImplementation(gradleTestKit())
}

tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
