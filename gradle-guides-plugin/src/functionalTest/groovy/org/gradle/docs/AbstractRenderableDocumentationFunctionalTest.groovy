package org.gradle.docs

import spock.lang.Unroll

abstract class AbstractRenderableDocumentationFunctionalTest extends AbstractFunctionalTest implements DocumentationTrait {
    def "defaults documentation element display name to title case"() {
        buildFile << applyDocumentationPlugin() << createDocumentationElement('foo') << createDocumentationElement('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${documentationDsl('foo')}.displayName.get() == 'Foo'
                    assert ${documentationDsl('fooBar')}.displayName.get() == 'Foo Bar'
                }
            }
        """

        expect:
        build('verify')
    }

    def "detaults documentation element description to empty string"() {
        buildFile << applyDocumentationPlugin() << createDocumentationElement('foo') << createDocumentationElement('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${documentationDsl('foo')}.description.get() == ''
                    assert ${documentationDsl('fooBar')}.description.get() == ''
                }
            }
        """

        expect:
        build('verify')
    }

    def "can detect dead links"() {
        given:
        makeSingleProject()
        writeDocumentationUnderTest()
        contentFileUnderTest << '''
            |https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4155278
            |https://youtrack.jetbrains.com/issue/TW-65268
            |https://not.existant/url
            |'''.stripMargin()

        expect:
        def result = buildAndFail(checkTaskNameUnderTest, "-i")
        result.output.contains('''   > The following links are broken:
      https://not.existant/url''')
    }

    protected abstract String getCheckTaskNameUnderTest()

    protected abstract String createDocumentationElement(String name)

    protected abstract String documentationDsl(String name)

    protected abstract void makeSingleProject()

    protected abstract void writeDocumentationUnderTest()

    protected abstract TestFile getContentFileUnderTest()
}
