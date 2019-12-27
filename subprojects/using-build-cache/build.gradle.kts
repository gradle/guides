plugins {
    id("org.gradle.guides")
    id("org.gradle.guides.test-jvm-code")
    kotlin("kapt") version "1.3.61" apply false
}

guide {
    repositoryPath.set("gradle-guides/using-build-cache")
    minimumGradleVersion.set("5.0")
    title.set("Using the Build Cache")
    category.set("Topical")
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
