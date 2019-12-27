package org.gradle.docs.guides

import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

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

    def "separate each rendered guides to individual workspace with snake case naming"() {
        buildFile << applyDocumentationPlugin() << createGuide('foo') << createGuide('fooBar')
        writeGuideUnderTest('src/docs/guides/foo')
        writeGuideUnderTest('src/docs/guides/foo-bar')

        when:
        assert !file("build/working/guides/docs/foo/index.adoc").exists()
        assert !file("build/working/guides/render-guides/foo/index.html").exists()
        assert !file("build/working/guides/docs/foo_bar/index.adoc").exists()
        assert !file("build/working/guides/render-guides/foo_bar/index.html").exists()
        def result = build('assemble')

        then:
        result.assertTasksExecutedAndNotSkipped(':generateFooPage', ':generateFooBarPage', ':assembleGuides', ':guidesMultiPage', ':assemble')

        and:
        file("build/working/guides/docs/foo/index.adoc").exists()
        file("build/working/guides/render-guides/foo/index.html").exists()

        and:
        file("build/working/guides/docs/foo_bar/index.adoc").exists()
        file("build/working/guides/render-guides/foo_bar/index.html").exists()
    }

    def "supports multiple adoc source file guides"() {
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/step-1.adoc') << """
== Step 1

Some information about step 1.
"""
        file('src/docs/guides/demo/contents/step-2.adoc') << """
== Step 2

Some information about step 2.
"""
        file('src/docs/guides/demo/contents/index.adoc') << """
include::step-1.adoc[]

include::step-2.adoc[]
"""

        when:
        def result = build('assemble')

        then:
        !result.output.contains('SEVERE: index.adoc:')

        and:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        (indexFile.text =~ /<h2 id="step_1"><a .+>Step 1<\/a><\/h2>/).find()
        (indexFile.text =~ /<h2 id="step_2"><a .+>Step 2<\/a><\/h2>/).find()
    }

    def "render only the index.adoc"() {
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/step-1.adoc') << """
== Step 1

Some information about step 1.
"""
        file('src/docs/guides/demo/contents/step-2.adoc') << """
== Step 2

Some information about step 2.
"""
        file('src/docs/guides/demo/contents/index.adoc') << """
include::step-1.adoc[]

include::step-2.adoc[]
"""

        when:
        !file('build/working/guides/render-guides/demo/index.html').exists()
        !file('build/working/guides/render-guides/demo/step-1.html').exists()
        !file('build/working/guides/render-guides/demo/step-2.html').exists()
        def result = build('assemble')

        then:
        file('build/working/guides/render-guides/demo/index.html').exists()
        !file('build/working/guides/render-guides/demo/step-1.html').exists()
        !file('build/working/guides/render-guides/demo/step-2.html').exists()
    }

    def "supports multiple text source file guides"() {
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/command-output-1.txt') << """
Some command output
"""
        file('src/docs/guides/demo/contents/command-output-2.txt') << """
Some other command output
"""
        file('src/docs/guides/demo/contents/index.adoc') << """
include::command-output-1.txt[]

include::command-output-2.txt[]
"""

        when:
        def result = build('assemble')

        then:
        !result.output.contains('SEVERE: index.adoc: ')

        and:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains('<p>Some command output</p>')
        indexFile.text.contains('<p>Some other command output</p>')
    }

    def "removes stale image source from rendered folder"() {
        makeSingleProject()
        writeGuideUnderTest()
        def image1 = image('src/docs/guides/demo/contents/images/image-1.png')
        def image2 = image('src/docs/guides/demo/contents/images/image-2.png')
        file('src/docs/guides/demo/contents/index.adoc') << """
image::image-1.png[]
"""

        when:
        !file('build/working/guides/render-guides/demo/images/image-1.png').exists()
        !file('build/working/guides/render-guides/demo/images/image-2.png').exists()
        build('assemble')

        then:
        file('build/working/guides/render-guides/demo/images/image-1.png').exists()
        file('build/working/guides/render-guides/demo/images/image-2.png').exists()

        when:
        image2.delete()
        build('assemble')

        then:
        file('build/working/guides/render-guides/demo/images/image-1.png').exists()
        !file('build/working/guides/render-guides/demo/images/image-2.png').exists()
    }

    def "can configure permalink of guides"() {
        makeSingleProject()
        writeGuideUnderTest()
        buildFile << """
            ${guideUnderTestDsl} {
                permalink = 'd-e-m-o'
            }
        """

        when:
        !file('build/working/guides/render-guides/demo/index.html').exists()
        !file('build/working/guides/render-guides/d-e-m-o/index.html').exists()
        build('assemble')

        then:
        !file('build/working/guides/render-guides/demo/index.html').exists()
        file('build/working/guides/render-guides/d-e-m-o/index.html').exists()
    }

    def "fails rendering on error"() {
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
include::step-1.adoc[]

include::step-2.adoc[]
"""

        expect:
        buildAndFail('assemble')
    }

    private TestFile image(Object... path) {
        def result = file(path)
        result.parentFile.mkdirs()

        def rnd = new Random()
        def image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_BGR)

        def array = ((DataBufferInt) image.getRaster().getDataBuffer()).getData()
        for (int i = 0; i < array.length; ++i) {
            array[i] = rnd.nextInt(0xFFFFFF)
        }

        ImageIO.write(image, "PNG", result)

        return result
    }
}
