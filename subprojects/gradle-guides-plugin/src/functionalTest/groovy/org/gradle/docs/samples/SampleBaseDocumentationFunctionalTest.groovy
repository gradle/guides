package org.gradle.docs.samples

import org.gradle.docs.AbstractBaseDocumentationFunctionalTest

class SampleBaseDocumentationFunctionalTest extends AbstractBaseDocumentationFunctionalTest implements SamplesTrait {
    @Override
    protected String createDocumentationElement(String name) {
        return createSample(name)
    }

    @Override
    protected String documentationDsl(String name) {
        return sampleDsl(name)
    }
}
