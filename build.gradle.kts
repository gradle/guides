plugins {
    id("com.gradle.build-scan") version "1.15.2"
    id("org.gradle.guides.getting-started") version "0.13.3"
    id("org.gradle.guides.test-jvm-code") version "0.13.3"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/writing-gradle-plugins"
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

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
