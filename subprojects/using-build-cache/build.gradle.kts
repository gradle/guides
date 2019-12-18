plugins {
    id("org.gradle.guides.topical") version "0.15.13"
    id("org.gradle.guides.test-jvm-code") version "0.15.13"
    kotlin("kapt") version "1.3.11" apply false
}

guide {
    repositoryPath.set("gradle-guides/using-build-cache")
    minimumGradleVersion.set("5.0")
    title.set("Using the Build Cache")
}

tasks {
    asciidoctor {
        resources(delegateClosureOf<CopySpec> {
            from("contents/css") {
                into("css")
            }
            from("contents/images") {
                into("images")
            }
        })
    }
}
