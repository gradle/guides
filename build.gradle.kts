plugins {
    `build-scan`
    id("org.gradle.guides.topical") version "0.15.1"
    id("org.gradle.guides.test-jvm-code") version "0.15.1"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/using-build-cache"
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
