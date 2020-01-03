package org.gradle.docs.guides

import org.gradle.docs.AbstractFunctionalTest

class AbstractGuideFunctionalSpec extends AbstractFunctionalTest implements GuidesTrait {
    protected void makeSingleProject() {
        buildFile << applyDocumentationPlugin() << createGuide('demo')
    }

    protected void writeGuideUnderTest(String directory="src/docs/guides/demo") {
        file("${directory}/contents/index.adoc") << """
= Demo

Some guide
"""
    }

    protected static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }

    protected String getGuideUnderTestDsl() {
        return guideDsl('demo')
    }
}
