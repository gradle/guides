import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.gradle.guides.topical") version "0.14.1"
    id("org.gradle.guides.test-jvm-code") version "0.14.1"
}

guide {
    repoPath = "gradle-guides/implementing-gradle-plugins"
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
