package org.gradle.docs

trait DocumentationTrait {
    static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }

            repositories {
                jcenter()
                maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
            }
        """
    }
}
