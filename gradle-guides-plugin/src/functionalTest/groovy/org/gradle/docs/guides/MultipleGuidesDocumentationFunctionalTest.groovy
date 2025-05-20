package org.gradle.docs.guides

class MultipleGuidesDocumentationFunctionalTest extends AbstractGuideFunctionalSpec {
    def "configures attributes per guides"() {
        buildFile << applyDocumentationPlugin() << createGuide('foo') << createGuide('bar')
        writeGuideUnderTest('src/docs/guides/foo')
        writeGuideUnderTest('src/docs/guides/bar')
        file('src/docs/guides/foo/contents/index.adoc') << '''
            |* Samples directory: {samples-dir}
            |* Samples code directory: {samplescodedir}
            |* Samples output directory: {samplesoutputdir}
            |'''.stripMargin()
        file('src/docs/guides/bar/contents/index.adoc') << '''
            |* Samples directory: {samples-dir}
            |* Samples code directory: {samplescodedir}
            |* Samples output directory: {samplesoutputdir}
            |'''.stripMargin()

        when:
        build('assemble')

        then:
        def indexFooFile = file('build/working/guides/render-guides/foo/index.html')
        indexFooFile.exists()
        indexFooFile.text.contains("Samples directory: ${file('src/docs/guides/foo/samples')}")
        indexFooFile.text.contains("Samples code directory: ${file('src/docs/guides/foo/samples/code')}")
        indexFooFile.text.contains("Samples output directory: ${file('src/docs/guides/foo/samples/output')}")

        and:
        def indexBarFile = file('build/working/guides/render-guides/bar/index.html')
        indexBarFile.exists()
        indexBarFile.text.contains("Samples directory: ${file('src/docs/guides/bar/samples')}")
        indexBarFile.text.contains("Samples code directory: ${file('src/docs/guides/bar/samples/code')}")
        indexBarFile.text.contains("Samples output directory: ${file('src/docs/guides/bar/samples/output')}")
    }
}
