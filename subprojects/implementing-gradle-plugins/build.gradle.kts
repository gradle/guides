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
    testImplementation("org.gradle:sample-check:0.6.1")
    testImplementation(gradleTestKit())
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
