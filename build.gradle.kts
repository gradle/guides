plugins {
    id("com.gradle.build-scan") version "2.3"
    id("org.gradle.guides.topical") version "0.15.7"
    id("org.gradle.guides.test-jvm-code") version "0.15.7"
    kotlin("kapt") version "1.3.11" apply false
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/using-build-cache"
    minimumGradleVersion.set("5.0")
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
