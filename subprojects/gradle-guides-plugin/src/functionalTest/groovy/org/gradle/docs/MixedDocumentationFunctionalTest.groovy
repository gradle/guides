package org.gradle.docs

import org.gradle.docs.guides.GuidesTrait
import org.gradle.docs.samples.SamplesTrait
import spock.lang.Ignore

class MixedDocumentationFunctionalTest extends AbstractFunctionalTest implements SamplesTrait, GuidesTrait {
    def "can assemble multiple documentation element type"() {
        buildFile << applyDocumentationPlugin() << createGuide('demoGuide') << createSampleWithBothDsl('demoSample')
        writeReadmeTo(file('src/docs/samples/demo-sample'))
        writeKotlinDslSampleTo(file('src/docs/samples/demo-sample/kotlin'))
        writeGroovyDslSampleTo(file('src/docs/samples/demo-sample/groovy'))
        writeContentTo(file('src/docs/guides/demo-guide'))

        when:
        def result = build('assemble')

        then:
        result.assertTasksExecutedAndNotSkipped(':generateDemoSamplePage', ':generateWrapperForSamples', ':zipSampleDemoSampleGroovy', ':zipSampleDemoSampleKotlin', ':assembleDemoSampleSample', ':generateDemoGuidePage', ':assembleGuides', ':guidesMultiPage', ':generateSampleIndex', ':installSampleDemoSampleGroovy', ':generateSanityCheckTests', ':installSampleDemoSampleGroovyForTest', ':installSampleDemoSampleKotlin', ':installSampleDemoSampleKotlinForTest', ':assembleSamples', ':samplesMultiPage', ':assemble')
    }

    @Ignore
    def "can create different documentation element type with the same name"() {
        buildFile << applyDocumentationPlugin() << createGuide('demo') << createSampleWithBothDsl('demo')
        writeReadmeTo(file('src/docs/samples/demo'))
        writeKotlinDslSampleTo(file('src/docs/samples/demo/kotlin'))
        writeGroovyDslSampleTo(file('src/docs/samples/demo/groovy'))
        writeContentTo(file('src/docs/guides/demo'))

        when:
        def result = build('assemble')

        then:
        result.assertTasksExecutedAndNotSkipped(':assemble')
    }

    protected static String applyDocumentationPlugin() {
        return """
            plugins {
                id 'org.gradle.documentation'
            }
        """
    }
}