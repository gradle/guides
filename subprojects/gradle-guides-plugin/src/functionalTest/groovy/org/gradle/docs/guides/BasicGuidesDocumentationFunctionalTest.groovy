package org.gradle.docs.guides

import org.gradle.docs.TestFile

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class BasicGuidesDocumentationFunctionalTest extends AbstractGuideFunctionalSpec {

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

    def "can render single page guide"() {
        makeSingleProject()
        writeGuideUnderTest()

        when:
        def result = build('assemble')

        then:
        result.assertTasksExecutedAndNotSkipped(':generateDemoPage', ':assembleGuides', ':guidesMultiPage', ':generateSampleIndex', ':assembleSamples', ':samplesMultiPage', ':assemble')
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
        result.assertTasksExecutedAndNotSkipped(':generateFooBarPage', ':generateFooPage', ':assembleGuides', ':guidesMultiPage', ':generateSampleIndex', ':assembleSamples', ':samplesMultiPage', ':assemble')

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
            |== Step 1
            |
            |Some information about step 1.
            |""".stripMargin()
        file('src/docs/guides/demo/contents/step-2.adoc') << """
            |== Step 2
            |
            |Some information about step 2.
            |""".stripMargin()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |include::step-1.adoc[]
            |
            |include::step-2.adoc[]
            |""".stripMargin()

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
            |== Step 1
            |
            |Some information about step 1.
            |""".stripMargin()
        file('src/docs/guides/demo/contents/step-2.adoc') << """
            |== Step 2
            |
            |Some information about step 2.
            |""".stripMargin()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |include::step-1.adoc[]
            |
            |include::step-2.adoc[]
            |""".stripMargin()

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
            |Some command output
            |""".stripMargin()
        file('src/docs/guides/demo/contents/command-output-2.txt') << """
            |Some other command output
            |""".stripMargin()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |include::command-output-1.txt[]
            |
            |include::command-output-2.txt[]
            |""".stripMargin()

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
            |image::image-1.png[]
            |""".stripMargin()

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

    def "adds Asciidoctor attributes for samples code and output directory"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |* Samples directory: {samples-dir}
            |* Samples code directory: {samplescodedir}
            |* Samples output directory: {samplesoutputdir}
            |""".stripMargin()

        when:
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains("Samples directory: ${file('src/docs/guides/demo/samples')}")
        indexFile.text.contains("Samples code directory: ${file('src/docs/guides/demo/samples/code')}")
        indexFile.text.contains("Samples output directory: ${file('src/docs/guides/demo/samples/output')}")
    }

    def "defaults guide repository path to guide name under gradle-guides organization"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        buildFile << """
            tasks.register('verifyGuideRepositoryPath') {
                doLast {
                    assert ${guideUnderTestDsl}.repositoryPath.get() == 'gradle-guides/demo'
                }
            }
        """

        expect:
        build('verifyGuideRepositoryPath')
    }

    def "can configure the repository path of the guide"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |* Repository path: {repository-path}
            |""".stripMargin()
        buildFile << """
            ${guideUnderTestDsl} {
                repositoryPath = "foo/bar"
            }
        """

        when:
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains('Repository path: foo/bar')
    }

    def "defaults to current Gradle version for minimum Gradle version of the guide"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |* Gradle version: {gradle-version}
            |* User manual link: {user-manual}
            |* Language reference link: {language-reference}
            |* API reference link: {api-reference}
            |""".stripMargin()

        when:
        usingGradleVersion('6.0')
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains('Gradle version: 6.0')
        (indexFile.text =~ /User manual link: <a href=.+>https:\/\/docs.gradle.org\/6\.0\/userguide\/<\/a>/).find()
        (indexFile.text =~ /Language reference link: <a href=.+>https:\/\/docs.gradle.org\/6\.0\/dsl\/<\/a>/).find()
        (indexFile.text =~ /API reference link: <a href=.+>https:\/\/docs.gradle.org\/6\.0\/javadoc\/<\/a>/).find()
    }

    def "can configure the minimum Gradle version of the guide"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |* Gradle version: {gradle-version}
            |* User manual link: {user-manual}
            |* Language reference link: {language-reference}
            |* API reference link: {api-reference}
            |""".stripMargin()
        buildFile << """
            ${guideUnderTestDsl} {
                minimumGradleVersion = "5.2"
            }
        """

        when:
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains('Gradle version: 5.2')
        (indexFile.text =~ /User manual link: <a href=.+>https:\/\/docs.gradle.org\/5\.2\/userguide\/<\/a>/).find()
        (indexFile.text =~ /Language reference link: <a href=.+>https:\/\/docs.gradle.org\/5\.2\/dsl\/<\/a>/).find()
        (indexFile.text =~ /API reference link: <a href=.+>https:\/\/docs.gradle.org\/5\.2\/javadoc\/<\/a>/).find()
    }

    def "can reference attributes for samples directories in Asciidoc generation"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |My build file:
            |include::{samplescodedir}/helloWorld/build.gradle[]
            |Output:
            |include::{samplesoutputdir}/helloWorld/build.out[]
            |""".stripMargin()
        def samplesCodeFolder = temporaryFolder.newFolder('src', 'docs', 'guides', 'demo', 'samples', 'code')
        File codeDir = createDir(samplesCodeFolder, 'helloWorld')
        new File(codeDir, 'build.gradle') << """
            task helloWorld {
                doLast {
                    println 'Hello world!'
                }
            }
        """
        def samplesOutputFolder = temporaryFolder.newFolder('src', 'docs', 'guides', 'demo', 'samples', 'output')
        File outputDir = createDir(samplesOutputFolder, 'helloWorld')
        new File(outputDir, 'build.out') << """
            |> Task :helloWorld
            |Hello world!
            |""".stripMargin()

        when:
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains('task helloWorld')
        indexFile.text.contains('Task :helloWorld')
    }

    def "header and footer is injected during asciidoctor postprocessing"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()

        when:
        build('assemble')

        then:
        def indexFile = file('build/working/guides/render-guides/demo/index.html')
        indexFile.exists()
        indexFile.text.contains('<script defer src="https://guides.gradle.org/js/guides')
        indexFile.text.contains('<header class="site-layout__header site-header js-site-header" itemscope="itemscope" itemtype="https://schema.org/WPHeader">')
        indexFile.text.contains('<footer class="site-layout__footer site-footer" itemscope="itemscope" itemtype="https://schema.org/WPFooter">')
    }

    def "can include contribution"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << """
            |include::contribute[repo-path="gradle-guides/demo"]
            |""".stripMargin()

        expect:
        build('assemble')
    }

    def "can include sample"() {
        given:
        makeSingleProject()
        writeGuideUnderTest()
        file('src/docs/guides/demo/contents/index.adoc') << '''
            |====
            |include::sample[dir="groovy-dsl/code", files="settings.gradle[]"]
            |include::sample[dir="kotlin-dsl/code", files="settings.gradle.kts[]"]
            |====
            |'''.stripMargin()
        temporaryFolder.newFolder('samples', 'groovy-dsl', 'code')
        temporaryFolder.newFolder('samples', 'kotlin-dsl', 'code')
        file('src/docs/guides/demo/samples/groovy-dsl/code/settings.gradle') << "rootProject.name = 'demo'"
        file('src/docs/guides/demo/samples/kotlin-dsl/code/settings.gradle.kts') << 'rootProject.name = "demo"'

        expect:
        build('assemble')
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
