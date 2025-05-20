package org.gradle.docs.samples

import org.gradle.docs.AbstractRenderableDocumentationFunctionalTest
import org.gradle.docs.TestFile

class SamplesRenderableDocumentationFunctionalTest extends AbstractRenderableDocumentationFunctionalTest implements SamplesTrait {
    @Override
    protected String createDocumentationElement(String name) {
        return createSampleWithBothDsl(name)
    }

    @Override
    protected String documentationDsl(String name) {
        return sampleDsl(name)
    }

    @Override
    protected void makeSingleProject() {
        buildFile << applyDocumentationPlugin() << createSampleWithBothDsl('demo')
    }

    @Override
    protected void writeDocumentationUnderTest() {
        writeReadmeTo(file('src/docs/samples/demo'))
        writeGroovyDslSampleTo(file('src/docs/samples/demo/groovy'))
        writeKotlinDslSampleTo(file('src/docs/samples/demo/kotlin'))
    }

    @Override
    protected TestFile getContentFileUnderTest() {
        return file('src/docs/samples/demo/README.adoc')
    }

    @Override
    protected String getCheckTaskNameUnderTest() {
        return 'checkDemoSampleLinks'
    }
}
