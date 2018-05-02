initscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.gradle:build-scan-plugin:1.13.1")
    }
}

rootProject {
    apply<com.gradle.scan.plugin.BuildScanPlugin>()

    configure<com.gradle.scan.plugin.BuildScanExtension> {
        setTermsOfServiceUrl("https://gradle.com/terms-of-service")
        setTermsOfServiceAgree("yes")
    }
}
