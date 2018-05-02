plugins {
    `build-scan`
    id("org.gradle.guides.topical") version "0.12.0"

    // Uncomment this line if you need test JVM code snippets
    // id("org.gradle.guides.test-jvm-code") version "0.11.5"
}

configure<org.gradle.guides.GuidesExtension> {
    repoPath = "gradle-guides/migrating-build-logic-from-groovy-to-kotlin"
    mainAuthor = "EDIT build.gradle TO ADD AUTHOR"
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
