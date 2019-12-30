package org.gradle.docs

abstract class AbstractWellBehavePluginFunctionalTest extends AbstractFunctionalTest {
    def "applying plugin should not throw exception"() {
        buildFile << """
            plugins {
                id '${pluginIdUnderTest}'
            }
        """

        expect:
        build('help')
    }

    protected abstract String getPluginIdUnderTest()
}
