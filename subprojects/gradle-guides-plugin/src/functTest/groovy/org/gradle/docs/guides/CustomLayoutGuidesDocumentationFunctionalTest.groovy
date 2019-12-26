package org.gradle.docs.guides

class CustomLayoutGuidesDocumentationFunctionalTest extends AbstractGuideFunctionalSpec {
    def "can relocate guide directory"() {
        makeSingleProject()
        writeGuideUnderTest('.')
        buildFile << """
            ${guideUnderTestDsl} {
                guideDirectory = projectDir
            }
        """

        when:
        def result = build("assemble")

        then:
        result.assertTasksExecutedAndNotSkipped(':generateDemoPage', ':assembleGuides', ':guidesMultiPage', ':assemble')
    }
}
