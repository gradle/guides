import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("com.gradle.build-scan") version "1.15.2"
    id("org.gradle.guides.getting-started") version "0.13.3"
    id("org.gradle.guides.test-jvm-code") version "0.13.3"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/writing-gradle-tasks"
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
