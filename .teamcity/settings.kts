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

version = "2021.1"

project {
    // parentId("DocumentationPortal")
    buildType(BuildPluginsOnLinux)
    buildType(BuildPluginsOnMac)
    buildType(PublishPlugins)
}

open class AbstractBuildType(init: BuildType.() -> Unit) : BuildType({
    vcs {
        root(DslContext.settingsRoot)
    }
    params {
        param("env.LC_ALL", "en_US.UTF-8")
        param("env.GRADLE_CACHE_REMOTE_URL", "%gradle.cache.remote.url%")
        param("env.GRADLE_CACHE_REMOTE_USERNAME", "%gradle.cache.remote.username%")
        param("env.GRADLE_CACHE_REMOTE_PASSWORD", "%gradle.cache.remote.password%")
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
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

open class AbstractBuildPluginType(init: BuildType.() -> Unit) : AbstractBuildType({
    steps {
        gradle {
            useGradleWrapper = true
            tasks = ":gradle-guides-plugin:check"
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

object BuildPluginsOnLinux : AbstractBuildPluginType({
    name = "Build All Plugins (Linux)"

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.JAVA_HOME", "%linux.java11.oracle.64bit%")
    }
    artifactRules = """
        build/published-guides/** => published-guides
    """.trimIndent()
})

object BuildPluginsOnMac : AbstractBuildPluginType({
    name = "Build All Plugins (macOS)"

    requirements {
        startsWith("teamcity.agent.jvm.os.name", "Mac")
    }
    params {
        param("env.JAVA_HOME", "%macos.java11.openjdk.64bit%")
    }
})

object BuildPluginsOnWindows : AbstractBuildPluginType({
    name = "Build All Plugins (Windows)"

    requirements {
        startsWith("teamcity.agent.jvm.os.name", "Windows")
    }
    params {
        param("env.JAVA_HOME", "%windows.java11.oracle.64bit%")
    }
})

object PublishPlugins : AbstractBuildType({
    name = "Publish Documentation Plugins"
    steps {
        gradle {
            useGradleWrapper = true
            gradleParams = "-Dgradle.publish.skip.namespace.check=true"
            tasks = ":gradle-guides-plugin:publishPlugins"
            buildFile = "" // Let Gradle detect the build script
        }
    }
    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
    params {
        param("env.JAVA_HOME", "%linux.java11.oracle.64bit%")
        password("env.GRADLE_PUBLISH_KEY", "%plugin.portal.publish.key%", display = ParameterDisplay.HIDDEN)
        password("env.GRADLE_PUBLISH_SECRET", "%plugin.portal.publish.secret%", display = ParameterDisplay.HIDDEN)
    }
})

