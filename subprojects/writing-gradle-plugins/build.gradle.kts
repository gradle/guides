plugins {
    id("org.gradle.guides")
    id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/writing-gradle-plugins")
    minimumGradleVersion.set("4.10.3")
    category.set("Getting Started")
}

tasks {
    "asciidoctor"(org.asciidoctor.gradle.AsciidoctorTask::class) {
        attributes(
            mapOf(
                "exampledir" to file("samples/code"),
                "gradle-outdir" to file("samples/output")
            )
        )
    }
}
