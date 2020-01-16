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
        result.assertTasksExecutedAndNotSkipped(':generateDemoPage', ':assembleGuides', ':guidesMultiPage', ':generateSampleIndex', ':assembleSamples', ':samplesMultiPage', ':assemble')
    }

    def "relocates Asciidoctor attributes for samples code and output directory"() {
        given:
        makeSingleProject()
        writeGuideUnderTest('custom-location')
        file('custom-location/contents/index.adoc') << """
            |* Samples directory: {samples-dir}
            |* Samples code directory: {samplescodedir}
            |* Samples output directory: {samplesoutputdir}
            |""".stripMargin()
        buildFile << """
            ${guideUnderTestDsl} {
                guideDirectory = file('custom-location')
            }
        """

        when:
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains("Samples directory: ${file('custom-location/samples')}")
        indexFile.text.contains("Samples code directory: ${file('custom-location/samples/code')}")
        indexFile.text.contains("Samples output directory: ${file('custom-location/samples/output')}")
    }
}
