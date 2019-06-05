package org.gradle.guides

import org.gradle.testkit.runner.TaskOutcome
import org.yaml.snakeyaml.Yaml

class GitHubPluginFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        buildFile << """
            plugins {
                id 'org.gradle.guides.github'
            }
        """
    }

    def "can generate dco.yml configuration file"() {
        def dcoDotYml = new File(projectDir, ".github/dco.yml")

        expect:
        !dcoDotYml.exists()

        def result = build("configureGitHub")
        result.task(":generateDeveloperCertificateOfOriginConfiguration").outcome == TaskOutcome.SUCCESS

        dcoDotYml.exists()
        new Yaml().load(dcoDotYml.newReader()).require.members == false
    }

    def "can generate .gitignore configuration file"() {
        def dotGitIgnore = new File(projectDir, ".gitignore")

        expect:
        !dotGitIgnore.exists()
        build("configureGitHub").task(":generateGitIgnoreConfiguration").outcome == TaskOutcome.SUCCESS
        dotGitIgnore.exists()
    }
}
