package org.gradle.docs.guides

import org.gradle.docs.AbstractFunctionalTest
import org.gradle.docs.DocumentationTrait

class AbstractGuideFunctionalSpec extends AbstractFunctionalTest implements GuidesTrait, DocumentationTrait {
    protected void makeSingleProject() {
        buildFile << applyDocumentationPlugin() << createGuide('demo')
    }

    protected void writeGuideUnderTest(String directory="src/docs/guides/demo") {
        file("${directory}/contents/index.adoc") << """
= Demo

Some guide
"""
    }

    protected String getGuideUnderTestDsl() {
        return guideDsl('demo')
    }
}
