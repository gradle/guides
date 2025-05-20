package org.gradle.docs

trait DocumentationTrait {
    static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }

            repositories {
                mavenCentral()
                maven { url = uri("https://repo.gradle.org/gradle/public") }
            }
        """
    }
}
