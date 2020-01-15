package org.gradle.docs.snippets

import org.gradle.docs.AbstractArchiveDocumentationFunctionalTest
import org.gradle.docs.TestFile

class SnippetsArchiveDocumentationFunctionalTest extends AbstractArchiveDocumentationFunctionalTest implements SnippetsTrait {
    @Override
    protected void makeSingleProject() {
        buildFile << applyDocumentationPlugin() << createSnippet('demo') << configureSnippetGroovyDsl('demo') << configureSnippetKotlinDsl('demo')
    }

    @Override
    protected void writeDocumentationUnderTest() {
        writeGroovyDslSnippetTo(file('src/docs/snippets/demo/groovy'))
        writeGroovyDslSnippetTo(file('src/docs/snippets/demo/kotlin'))
    }

    @Override
    protected List<String> getAllTaskToAssembleDocumentationUnderTest() {
        return allTaskToAssemble('demo')
    }

    @Override
    protected String getAssembleDocumentationUnderTestTaskName() {
        return assembleTaskName('demo')
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
