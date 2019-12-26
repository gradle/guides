package org.gradle.docs.guides

import spock.lang.Unroll

class BasicGuidesDocumentationFunctionalTest extends AbstractGuideFunctionalSpec {

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

    // TODO: Add this test for samples as well
    @Unroll
    def "fails if disallowed characters in guide name"(String name) {
        buildFile << applyDocumentationPlugin() << createGuide(name)

        expect:
        def result = buildAndFail('help')
        result.output.contains("Guide '${name}' has disallowed characters")

        where:
        name << ['foo_bar', 'foo-bar']
    }

    def "can render single page guide"() {
        makeSingleProject()
        writeGuideUnderTest()

        when:
        def result = build('assemble')

        then:
        result.assertTasksExecutedAndNotSkipped(':generateDemoPage', ':assembleGuides', ':guidesMultiPage', ':assemble')
    }
}
