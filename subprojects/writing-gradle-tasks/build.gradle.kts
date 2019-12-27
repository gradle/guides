import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.gradle.guides")
}

guide {
    repositoryPath.set("gradle-guides/writing-gradle-tasks")
    minimumGradleVersion.set("5.0")
    category.set("Getting Started")
}

tasks {
    getByName<AsciidoctorTask>("asciidoctor") {
        inputs.dir("samples")
        attributes(
            mapOf("groovy-example-dir" to file("samples/groovy-dsl"),
                  "kotlin-example-dir" to file("samples/kotlin-dsl"))
        )
    }
    getByName<AsciidoctorTask>("guidesMultiPage") {
        inputs.dir("samples")
        attributes(
                mapOf("groovy-example-dir" to file("samples/groovy-dsl"),
                        "kotlin-example-dir" to file("samples/kotlin-dsl"))
        )
    }
}
