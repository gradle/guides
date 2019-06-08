includeBuild("subprojects/gradle-guides-plugin")
if (file("subprojects").exists()) {
    file(".mrconfig").readLines().filter { it.startsWith("[") && it.endsWith("]") }.map { it.substring(1, it.length - 1) }.forEach {
        settings.includeBuild(it)
    }

    // Resolve to the gradle-guides-plugin subprojects instead of the released plugins
    val guidePluginIds = setOf("org.gradle.guides.base", "org.gradle.guides.getting-started", "org.gradle.guides.topical", "org.gradle.guides.tutorial", "org.gradle.guides.test-jvm-code")
    pluginManagement {
        resolutionStrategy {
            eachPlugin {
                if (guidePluginIds.contains(requested.id.id)) {
                    useModule("org.gradle.guides:gradle-guides-plugin")
                }
            }
        }
    }
}
