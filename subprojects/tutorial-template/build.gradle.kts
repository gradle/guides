plugins {
    id("org.gradle.guides.tutorial")

    // Uncomment this line if you need test JVM code snippets
    // id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/@@GUIDE_SLUG@@")
    minimumGradleVersion.set("EDIT build.gradle.kts TO ADD MINIMUM GRADLE VERSION")
}
