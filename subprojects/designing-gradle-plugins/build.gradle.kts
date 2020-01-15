plugins {
    id("org.gradle.guide")
}

guide {
    repositoryPath.set("gradle-guides/designing-gradle-plugins")
    minimumGradleVersion.set("4.10.3")
    category.set("Topical")
}

// TODO: Doesn't seems like there are any commands to test
tasks.named("checkAsciidoctorContents") {
    enabled = false
}
