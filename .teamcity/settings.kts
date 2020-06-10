import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

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

version = "2019.2"

project {
//    parentId("DocumentationPortal")
    buildType(BuildGuidesOnLinux)
    buildType(BuildGuidesOnMac)
    // TODO: Obviously, it fails on Windows
    //buildType(BuildGuidesOnWindows)
    buildType(PublishPlugins)
    buildType(PublishGuides)
}

open class AbstractBuildType(init: BuildType.() -> Unit) : BuildType({
    vcs {
        root(DslContext.settingsRoot)
    }
    params {
        param("env.LC_ALL", "en_US.UTF-8")
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = "DocumentationPortal_Guides_HttpsGithubComGradleGuidesRefsHeadsMaster"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:57b9c337-4279-44d6-b9db-58fcdb006249"
                }
            }
        }
    }

    init()
})

open class AbstractBuildGuideType(init: BuildType.() -> Unit) : AbstractBuildType({
    steps {
        gradle {
            useGradleWrapper = true
            tasks = "build --continue --build-cache -Dgradle.cache.remote.url=%gradle.cache.remote.url% -Dgradle.cache.remote.username=%gradle.cache.remote.username% -Dgradle.cache.remote.password=%gradle.cache.remote.password% -Dgradle.cache.remote.push=true"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    triggers {
        vcs {
            groupCheckinsByCommitter = true
            branchFilter = """
                +:*
                -:pull/*
            """.trimIndent()
        }
    }

    init()
})

object BuildGuidesOnLinux : AbstractBuildGuideType({
    name = "Build All Guides (Linux)"

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
    }
    steps {
        gradle {
            useGradleWrapper = true
            tasks = ":installGuides"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    artifactRules = """
        build/published-guides/** => published-guides
    """.trimIndent()
})

object BuildGuidesOnMac : AbstractBuildGuideType({
    name = "Build All Guides (macOS)"

    requirements {
        startsWith("teamcity.agent.jvm.os.name", "Mac")
    }
    params {
        param("env.JAVA_HOME", "%macos.java8.oracle.64bit%")
    }
})

object BuildGuidesOnWindows : AbstractBuildGuideType({
    name = "Build All Guides (Windows)"

    requirements {
        startsWith("teamcity.agent.jvm.os.name", "Windows")
    }
    params {
        param("env.JAVA_HOME", "%windows.java8.oracle.64bit%")
    }
})

object PublishPlugins : AbstractBuildType({
    name = "Publish Documentation Plugins"
    steps {
        gradle {
            useGradleWrapper = true
            gradleParams = "-Dgradle.publish.skip.namespace.check=true -Pgradle.publish.key=%GRADLE_PUBLISH_KEY% -Pgradle.publish.secret=%GRADLE_PUBLISH_SECRET%"
            tasks = "publishDocumentationPlugins"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        password("GRADLE_PUBLISH_KEY", "credentialsJSON:8b35ff9d-0de0-4266-8850-9162aa31b4bf", display = ParameterDisplay.HIDDEN)
        password("GRADLE_PUBLISH_SECRET", "credentialsJSON:6934a5f0-e002-4067-884e-5090de3c9ec3", display = ParameterDisplay.HIDDEN)
    }
})

object PublishGuides : AbstractBuildType({
    name = "Publish All Guides"
    steps {
        gradle {
            useGradleWrapper = true
            tasks = ":gitPublishPush"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        password("env.GRGIT_USER", "credentialsJSON:57b9c337-4279-44d6-b9db-58fcdb006249", display = ParameterDisplay.HIDDEN)
    }
})
