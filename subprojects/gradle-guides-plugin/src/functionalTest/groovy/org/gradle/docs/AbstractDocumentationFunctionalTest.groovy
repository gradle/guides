package org.gradle.docs

class AbstractDocumentationFunctionalTest extends AbstractFunctionalTest {
    protected static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }
}
