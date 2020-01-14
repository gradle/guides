plugins {
    id("org.gradle.guide")
    kotlin("kapt") version "1.3.61" apply false
}

guide {
    repositoryPath.set("gradle-guides/using-build-cache")
    minimumGradleVersion.set("5.0")
    displayName.set("Using the Build Cache")
    category.set("Topical")
}

tasks {
    guidesMultiPage {
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

// TODO: Seems to need seed sample
tasks.named("asciidoctorContentDocsTest") {
    enabled = false
}
