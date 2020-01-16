package org.gradle.docs.guides

import org.gradle.testkit.runner.TaskOutcome

class IncrementalGuidesDocumentationFunctionalTest extends AbstractGuideFunctionalSpec {
    def "asciidoctor is up-to-date on consecutive execution without change"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()

        when:
        def result = build('guidesMultiPage')
        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.SUCCESS

        when:
        result = build('guidesMultiPage', '--info')
        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.UP_TO_DATE

        when:
        file('src/docs/guides/demo/contents/index.adoc') << """
            |More content.
            |""".stripMargin()
        result = build('guidesMultiPage')
        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.SUCCESS
    }

    def "asciidoctor is out of date if samples change"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        def samplesCodeDir = temporaryFolder.newFolder('samples', 'code')
        def samplesOutputDir = temporaryFolder.newFolder('samples', 'output')

        when:
        def result = build('guidesMultiPage')

        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.SUCCESS

        when:
        new File(samplesCodeDir, "build.gradle") << 'apply plugin: java'
        result = build('guidesMultiPage')

        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.SUCCESS

        when:
        new File(samplesOutputDir, "my-task-output.log") << 'Build SUCCESSFUL'
        result = build('guidesMultiPage')

        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.SUCCESS

        when:
        result = build('guidesMultiPage', '--info')

        then:
        result.task(':guidesMultiPage').outcome == TaskOutcome.UP_TO_DATE
    }
}
