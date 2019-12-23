import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.1"

project {
    parentId("DocumentationPortal")
    buildType(BuildGuides)
    buildType(PublishPlugins)
    buildType(PublishGuides)
}

open class AbstractBuild(init: BuildType.() -> Unit) : BuildType({
    vcs {
        root(DslContext.settingsRoot)
    }
    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.LC_ALL", "en_US.UTF-8")
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
    }

    init()
})

object BuildGuides : AbstractBuild({
    name = "Build All Guides"
    steps {
        gradle {
            useGradleWrapper = true
            tasks = "build"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    triggers {
        vcs {
            branchFilter = """
                +:*
            """.trimIndent()
            groupCheckinsByCommitter = true
        }
    }
})

object PublishPlugins : AbstractBuild({
    name = "Publish Documentation Plugins"
    steps {
        gradle {
            useGradleWrapper = true
            gradleParams = "-Dgradle.publish.skip.namespace.check=true -Pgradle.publish.key=%GRADLE_PUBLISH_KEY% -Pgradle.publish.secret=%GRADLE_PUBLISH_SECRET%"
            tasks = "publishPlugins"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    params {
        password("GRADLE_PUBLISH_KEY", "credentialsJSON:9ea244eb-7e24-44c5-8d4d-0e4d512b1608", display = ParameterDisplay.HIDDEN)
        password("GRADLE_PUBLISH_SECRET", "credentialsJSON:14df9326-8326-4329-bb04-3837d010d2e8", display = ParameterDisplay.HIDDEN)
    }
})

object PublishGuides : AbstractBuild({
    name = "Publish All Guides"
    steps {
        gradle {
            useGradleWrapper = true
            tasks = "publishPlugins"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    params {
        password("env.GRGIT_USER", "credentialsJSON:c77b23f9-e0bf-4f4b-ba31-c54802542cca", display = ParameterDisplay.HIDDEN)
    }
})
