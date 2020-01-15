package org.gradle.docs.samples

import org.gradle.docs.AbstractArchiveDocumentationFunctionalTest
import org.gradle.docs.TestFile

class SamplesArchiveDocumentationFunctionalTest extends AbstractArchiveDocumentationFunctionalTest implements SamplesTrait {
    @Override
    protected void makeSingleProject() {
        buildFile << applyDocumentationPlugin() << createSample('demo') << configureSampleGroovyDsl('demo') << configureSampleKotlinDsl('demo')
    }

    @Override
    protected void writeDocumentationUnderTest() {
        writeReadmeTo(file('src/docs/samples/demo'))
        writeKotlinDslSampleTo(file('src/docs/samples/demo/kotlin'))
        writeGroovyDslSampleTo(file('src/docs/samples/demo/groovy'))
    }

    @Override
    protected String getAssembleDocumentationUnderTestTaskName() {
        return assembleTaskName('demo')
    }

    @Override
    protected List<String> getAllTaskToAssembleDocumentationUnderTest() {
        return allTaskToAssemble('demo')
    }

    @Override
    protected void assertDslZipFilesExists() {
        groovyDslZipFile(file('build'), 'demo').assertExists()
        kotlinDslZipFile(file('build'), 'demo').assertExists()
    }

    @Override
    protected List<TestFile> getDslZipFiles() {
        return [groovyDslZipFile(file('build'), 'demo'), kotlinDslZipFile(file('build'), 'demo')]
    }
}
