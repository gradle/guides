package org.gradle.docs

trait LegacyPluginTrait {
    static String applyLegacyPlugin() {
        return  """
            plugins {
                id 'org.gradle.guide'
            }

            repositories {
                jcenter()
                maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
            }
        """
    }
}
