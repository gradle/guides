package org.gradle.guides

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin to configure a guide's GitHub repository.
 *
 * @since 0.15.8
 */
@CompileStatic
class GitHubPlugin implements Plugin<Project> {
    static final String CONFIGURE_GITHUB_REPOSITORY_TASK_NAME = "configureGitHubRepository"

    @Override
    void apply(Project project) {
        def generateDcoConfiguration = project.tasks.register("generateDeveloperCertificateOfOriginConfiguration", GenerateDeveloperCertificateOfOriginConfiguration) {
            it.outputFile.set(project.layout.projectDirectory.file(".github/dco.yml"))
        }

        def generateGitIgnoreConfiguration = project.tasks.register("generateGitIgnoreConfiguration", GenerateGitIgnoreConfiguration) {
            it.outputFile.set(project.layout.projectDirectory.file(".gitignore"))
        }

        def configureGitHubRepository = project.tasks.register(CONFIGURE_GITHUB_REPOSITORY_TASK_NAME, ConfigureGitHubRepository)

        project.tasks.register("configureGitHub") {
            it.group = "Guide Setup"
            it.description = "Configure GitHub repository"
            it.dependsOn(generateDcoConfiguration, generateGitIgnoreConfiguration, configureGitHubRepository)
        }
    }
}
