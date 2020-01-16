package org.gradle.docs.guides

class PublishingGuidesDocumentationFunctionalTest extends AbstractGuideFunctionalSpec {
    def "can resolve rendered guides from legacy plugins"() {
        settingsFile << """
            include 'legacy'
        """
        buildFile << configurePublishProject() << """
            dependencies {
                guides project('legacy')
            }
        """
        file('legacy/build.gradle') << """
            plugins {
                id 'org.gradle.guide'
            }
        """
        writeGuideUnderTest("legacy")

        when:
        assert !file("build/published-guides/legacy/index.html").exists()
        def result = build('publish')

        then:
        file("build/published-guides/legacy/index.html").exists()
    }

    def "can resolve multiple rendered guides from single project using new documentation plugins"() {
        settingsFile << """
            include 'guides'
        """
        buildFile << configurePublishProject() << """
            dependencies {
                guides project('guides')
            }
        """
        file('guides/build.gradle') << applyDocumentationPlugin() << createGuide('foo') << createGuide('bar')
        writeGuideUnderTest('guides/src/docs/guides/foo')
        writeGuideUnderTest('guides/src/docs/guides/bar')

        when:
        assert !file("build/published-guides/foo/index.html").exists()
        assert !file("build/published-guides/bar/index.html").exists()
        def result = build('publish')

        then:
        file("build/published-guides/foo/index.html").exists()
        file("build/published-guides/bar/index.html").exists()
    }

    def "can resolve multiple rendered guides from multiple projects using new documentation plugins"() {
        settingsFile << """
            include 'foo', 'bar'
        """
        buildFile << configurePublishProject() << """
            dependencies {
                guides project('foo')
                guides project('bar')
            }
        """
        file('foo/build.gradle') << applyDocumentationPlugin() << createGuide('foo')
        writeGuideUnderTest('foo/src/docs/guides/foo')
        file('bar/build.gradle') << applyDocumentationPlugin() << createGuide('bar')
        writeGuideUnderTest('bar/src/docs/guides/bar')

        when:
        assert !file("build/published-guides/foo/index.html").exists()
        assert !file("build/published-guides/bar/index.html").exists()
        def result = build('publish')

        then:
        file("build/published-guides/foo/index.html").exists()
        file("build/published-guides/bar/index.html").exists()
    }

    def "can resolve multiple rendered guides from multiple projects using both new documentation and legacy plugins"() {
        settingsFile << """
            include 'legacy', 'foo'
        """
        buildFile << configurePublishProject() << """
            dependencies {
                guides project('legacy')
                guides project('foo')
            }
        """
        file('legacy/build.gradle') << """
            plugins {
                id 'org.gradle.guide'
            }
        """
        writeGuideUnderTest('legacy')
        file('foo/build.gradle') << applyDocumentationPlugin() << createGuide('foo')
        writeGuideUnderTest('foo/src/docs/guides/foo')

        when:
        assert !file("build/published-guides/legacy/index.html").exists()
        assert !file("build/published-guides/foo/index.html").exists()
        def result = build('publish')

        then:
        file("build/published-guides/legacy/index.html").exists()
        file("build/published-guides/foo/index.html").exists()
    }

    private static String configurePublishProject() {
        return '''
            configurations {
                guides {
                    canBeResolved = true
                    canBeConsumed = false
                    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, 'docs'))
                    attributes.attribute(Attribute.of('type', String), 'guide-docs')
                }
            }
            tasks.register('publish', Sync) {
                from(configurations.guides)
                into("$buildDir/published-guides")
            }
        '''
    }
}
