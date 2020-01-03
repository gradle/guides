package org.gradle.docs

import spock.lang.Unroll

abstract class AbstractBaseDocumentationFunctionalTest extends AbstractFunctionalTest {
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

    @Unroll
    def "fails if disallowed characters in documentation element name (#name)"(String name) {
        buildFile << applyDocumentationPlugin() << createDocumentationElement(name)

        expect:
        def result = buildAndFail('help')
        result.output.contains("'${name}' has disallowed characters")

        where:
        name << ['foo_bar', 'foo-bar']
    }

    protected static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }
    protected abstract String createDocumentationElement(String name)

    protected abstract String documentationDsl(String name)
}
