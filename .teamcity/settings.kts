import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.version

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
    vcsRoot(GradleGuidesPluginBranches)
    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(GradleGuidesPluginBranches)
    }
    steps {
        gradle {
            useGradleWrapper = true
            tasks = "build"
        }
    }
    triggers {
        vcs {
            groupCheckinsByCommitter = true
        }
    }
    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.LC_ALL", "en_US.UTF-8")
        param("env.JAVA_HOME", "%linux.java8.openjdk.64bit%")
    }
})

object GradleGuidesPluginBranches : GitVcsRoot({
    uuid = "022ee0e9-d93c-4b47-acbe-2b476820aa41"
    name = "Gradle Guides Plugin Branches"
    url = "https://github.com/gradle-guides/gradle-guides-plugin.git"
    branchSpec = "+:refs/heads/*"
    agentGitPath = "%env.TEAMCITY_GIT_PATH%"
    useMirrors = false
    authMethod = password {
        userName = "gradlewaregitbot"
        password = "credentialsJSON:5306bfc7-041e-46e8-8d61-1d49424e7b04"
    }
})
