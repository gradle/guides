plugins {
    id("org.gradle.guides.topical") version "0.15.13"

    // Uncomment this line if you need test JVM code snippets
    // id("org.gradle.guides.test-jvm-code") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/@@GUIDE_SLUG@@")
    minimumGradleVersion.set("EDIT build.gradle.kts TO ADD MINIMUM GRADLE VERSION")
}
