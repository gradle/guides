package org.gradle.guides

import org.gradle.api.Plugin
import org.gradle.api.Project

class SetupPlugin implements Plugin<Project> {
    static final String SETUP_GUIDE_TASK_NAME = "setupGuide"
    private static final String GUIDE_SETUP_GROUP_NAME = "Guide Setup"

    @Override
    void apply(Project project) {
        def generateEditorConfigurationTask = project.tasks.register("generateEditorConfiguration", GenerateEditorConfiguration) {
            it.outputFile.set(project.file(".editorconfig"))
        }

        def generateLicenseFileTask = project.tasks.register("generateLicenseFile", GenerateLicenseFile) {
            it.outputFile.set(project.file("LICENSE"))
        }

        def generateReadMeFileTask = project.tasks.register("generateReadMeFile", GenerateReadMeFile) {
            it.outputFile.set(project.file("README.adoc"))
        }

        def generateDcoConfigurationTask = project.tasks.register("generateDeveloperCertificateOfOriginConfiguration", GenerateDeveloperCertificateOfOriginConfiguration) {
            it.outputFile.set(project.layout.projectDirectory.file(".github/dco.yml"))
        }

        def generateGitIgnoreConfigurationTask = project.tasks.register("generateGitIgnoreConfiguration", GenerateGitIgnoreConfiguration) {
            it.outputFile.set(project.layout.projectDirectory.file(".gitignore"))
        }

        def generateCodeOfConductFileTask = project.tasks.register("generateCodeOfConductFile", GenerateCodeOfConductFile) {
            it.outputFile.set(project.layout.projectDirectory.file(".github/CODE_OF_CONDUCT.md"))
        }

        def setupGuideTask = project.tasks.register(SETUP_GUIDE_TASK_NAME) {
            it.group = GUIDE_SETUP_GROUP_NAME
            it.description = "Configure Guide Repository"
            it.dependsOn(generateEditorConfigurationTask, generateLicenseFileTask, generateReadMeFileTask, generateDcoConfigurationTask, generateGitIgnoreConfigurationTask, generateCodeOfConductFileTask)
        }
    }
}
