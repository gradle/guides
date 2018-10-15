plugins {
    id("com.gradle.build-scan") version "1.15.2"
    id("org.gradle.guides.topical") version "0.14.1"
    id("org.gradle.guides.test-jvm-code") version "0.14.1"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/using-build-cache"
}

dependencies {
    testImplementation("org.spockframework:spock-core:1.2-groovy-2.5")
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
