import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.gradle.guides.getting-started")
    id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/writing-gradle-tasks")
    minimumGradleVersion.set("5.0")
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
