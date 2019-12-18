plugins {
    id("org.gradle.guides.getting-started") version "0.15.13"
}

guide {
    repositoryPath.set("gradle-guides/@@GUIDE_SLUG@@")
    minimumGradleVersion.set("EDIT build.gradle.kts TO ADD MINIMUM GRADLE VERSION")
}
