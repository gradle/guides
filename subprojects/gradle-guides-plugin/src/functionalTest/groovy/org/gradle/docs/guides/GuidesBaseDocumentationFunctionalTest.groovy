package org.gradle.docs.guides

import org.gradle.docs.AbstractBaseDocumentationFunctionalTest

class GuidesBaseDocumentationFunctionalTest extends AbstractBaseDocumentationFunctionalTest implements GuidesTrait {
    @Override
    protected String createDocumentationElement(String name) {
        return createGuide(name)
    }

    @Override
    protected String documentationDsl(String name) {
        return guideDsl(name)
    }
}
