package org.gradle.docs.guides

class BasicGuidesDocumentationFunctionalTest extends AbstractFunctionalTest {

    def "verify guide description default values"() {
        buildFile << applyDocumentationPlugin() << createGuide('foo') << createGuide('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${guideDsl('foo')}.description.get() == ''
                    assert ${guideDsl('fooBar')}.description.get() == ''
                }
            }
        """

        expect:
        build('verify')
    }

    def "verify guide title default values"() {
        buildFile << applyDocumentationPlugin() << createGuide('foo') << createGuide('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${guideDsl('foo')}.title.get() == 'Foo'
                    assert ${guideDsl('fooBar')}.title.get() == 'Foo Bar'
                }
            }
        """

        expect:
        build('verify')
    }

    def "verify guide repository path default values"() {
        buildFile << applyDocumentationPlugin() << createGuide('foo') << createGuide('fooBar')
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert ${guideDsl('foo')}.repositoryPath.get() == 'gradle-guides/foo'
                    assert ${guideDsl('fooBar')}.repositoryPath.get() == 'gradle-guides/foo-bar'
                }
            }
        """

        expect:
        build('verify')
    }

    protected void makeSingleProject() {
        buildFile << applyDocumentationPlugin() << createGuide('demo')
    }

    protected static String applyDocumentationPlugin() {
        return  """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }
    protected static String createGuide(String name) {
        return """
            documentation.guides.publishedGuides.create('${name}')
        """
    }

    protected String getGuideUnderTestDsl() {
        return guideDsl('demo')
    }

    protected static String guideDsl(String name) {
        return "documentation.guides.publishedGuides.${name}"
    }
}
