import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    `build-scan`
    id("org.gradle.guides.topical") version "0.15.8"
    id("org.gradle.guides.test-jvm-code") version "0.15.8"
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    testImplementation("org.gradle:sample-check:0.6.1")
    testImplementation(gradleTestKit())
}

guide {
    repositoryPath.set("gradle-guides/implementing-gradle-plugins")
    minimumGradleVersion.set("5.0")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}

tasks {
    getByName<AsciidoctorTask>("asciidoctor") {
        inputs.dir("samples")
        attributes(
            mapOf("groovy-example-dir" to file("samples/groovy-dsl"),
                  "kotlin-example-dir" to file("samples/kotlin-dsl"))
        )
    }
}
