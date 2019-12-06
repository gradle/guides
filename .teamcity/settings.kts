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
    buildType(Build)
    buildType(Release)
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

object Build : AbstractBuild({
    name = "Build"
    steps {
        gradle {
            useGradleWrapper = true
            tasks = "build"
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

object Release : AbstractBuild({
    name = "Release"
    steps {
        gradle {
            useGradleWrapper = true
            gradleParams = "-Dgradle.publish.skip.namespace.check=true -Pgradle.publish.key=%GRADLE_PUBLISH_KEY% -Pgradle.publish.secret=%GRADLE_PUBLISH_SECRET%"
            tasks = "release"
        }
    }
    params {
        password("GRADLE_PUBLISH_KEY", "credentialsJSON:13d96e17-375d-4753-aaea-caa5b3f748ec", display = ParameterDisplay.HIDDEN)
        password("GRADLE_PUBLISH_SECRET", "credentialsJSON:13245140-f526-4aff-8e98-efe67142912a", display = ParameterDisplay.HIDDEN)
    }
})
