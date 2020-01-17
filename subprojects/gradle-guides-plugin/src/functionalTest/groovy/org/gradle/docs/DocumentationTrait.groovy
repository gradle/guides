package org.gradle.docs

trait DocumentationTrait {
    static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }
}