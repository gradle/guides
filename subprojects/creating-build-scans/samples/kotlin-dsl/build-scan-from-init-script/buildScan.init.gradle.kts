initscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.gradle:build-scan-plugin:@scanPluginVersion@")
    }
}

rootProject {
    apply<com.gradle.scan.plugin.BuildScanPlugin>()

    configure<com.gradle.scan.plugin.BuildScanExtension> {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
