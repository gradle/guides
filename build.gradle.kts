plugins {
    id("org.gradle.guides.topical") version "0.15.11"
    id("com.gradle.build-scan") version "2.3"

    // Uncomment this line if you need test JVM code snippets
    // id("org.gradle.guides.test-jvm-code") version "0.15.11"
}

guide {
    repositoryPath.set("gradle-guides/@@GUIDE_SLUG@@")
    minimumGradleVersion.set("EDIT build.gradle.kts TO ADD MINIMUM GRADLE VERSION")
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
