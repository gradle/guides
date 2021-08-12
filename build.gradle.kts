import org.gradle.docs.internal.exemplar.AsciidoctorContentTest

plugins {
    id("org.ajoberstar.git-publish") version("2.1.3")
    id("org.gradle.documentation") apply(false)
}

// When removing a guide that has been linked from elsewhere before, please add a redirect here
val redirects = mapOf(
    "building-cpp-applications" to "https://docs.gradle.org/current/samples/sample_building_cpp_applications.html",
    "building-cpp-libraries" to "https://docs.gradle.org/current/samples/sample_building_cpp_libraries.html",
    "building-groovy-libraries" to " https://docs.gradle.org/current/samples/sample_building_groovy_libraries.html",
    "building-java-9-modules" to "https://docs.gradle.org/current/samples/sample_java_modules_multi_project.html",
    "building-java-applications" to "https://docs.gradle.org/current/samples/sample_building_java_applications.html",
    "building-java-libraries" to "https://docs.gradle.org/current/samples/sample_building_java_libraries.html",
    "building-java-web-applications" to "https://gretty-gradle-plugin.github.io/gretty-doc",
    "building-kotlin-jvm-libraries" to "https://docs.gradle.org/current/samples/sample_building_kotlin_libraries.html",
    "building-scala-libraries" to "https://docs.gradle.org/current/samples/sample_building_scala_libraries.html",
    "building-spring-boot-2-projects-with-gradle" to "https://spring.io/guides/gs/spring-boot",
    "building-swift-applications" to "https://docs.gradle.org/current/samples/sample_building_swift_applications.html",
    "building-swift-libraries" to "https://docs.gradle.org/current/samples/sample_building_swift_libraries.html",
    "creating-build-scans" to "https://scans.gradle.com",
    "creating-multi-project-builds" to "https://docs.gradle.org/current/samples/sample_building_java_applications_multi_project.html",
    "creating-new-gradle-builds" to "https://docs.gradle.org/current/samples",
    "consuming-jvm-libraries" to "https://docs.gradle.org/current/samples/sample_building_java_applications.html",
    "writing-gradle-tasks" to "https://docs.gradle.org/current/userguide/custom_tasks.html",

    "executing-gradle-builds-on-jenkins" to "https://docs.gradle.org/current/userguide/jenkins.html",
    "executing-gradle-builds-on-teamcity" to "https://docs.gradle.org/current/userguide/teamcity.html",
    "executing-gradle-builds-on-travisci" to "https://docs.gradle.org/current/userguide/travis-ci.html",
    "performance" to "https://docs.gradle.org/current/userguide/performance.html",
    "using-build-cache" to "https://docs.gradle.org/current/userguide/build_cache_use_cases.html",
    "designing-gradle-plugins" to "https://docs.gradle.org/current/userguide/designing_gradle_plugins.html",
    "implementing-gradle-plugins" to "https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html",
    "testing-gradle-plugins" to "https://docs.gradle.org/current/userguide/testing_gradle_plugins.html",
    "publishing-plugins-to-gradle-plugin-portal" to "https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html",
    "migrating-build-logic-from-groovy-to-kotlin" to "https://docs.gradle.org/current/userguide/migrating_from_groovy_to_kotlin_dsl.html",
    "using-the-worker-api" to "https://docs.gradle.org/current/userguide/worker_api.html"
)

tasks.register("clean") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

tasks.register("publishDocumentationPlugins") {
    dependsOn(gradle.includedBuild("gradle-guides-plugin").task(":publishPlugins"))
}

// Install guides into a single repository
val guides by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "docs"))
    attributes.attribute(Attribute.of("type", String::class.java), "guide-docs")
}
val installGuides = tasks.register("installGuides", Sync::class.java) {
    from(guides)
    from("js") { into("js") }
    from("icon") { into("icon") }
    from("css") { into("css") }
    from("CNAME")
    from("rebots.txt")
    from("google0c2dba1d0e25f4f9.html")
    from("gradle-guides.svg")
    into("$buildDir/published-guides")

    doLast {
        file("$buildDir/published-guides/.nojekyll")

        redirects.forEach { (from, to) ->
            val indexHtml = file("$buildDir/published-guides/$from/index.html").apply { parentFile.mkdirs() }
            indexHtml.writeText("""
                    <!DOCTYPE html>
                    <meta charset="utf-8">
                    <title>Redirecting to $to</title>
                    <meta http-equiv="refresh" content="0; URL=$to">
                    <link rel="canonical" href="$to">
                """.trimIndent()
            )
        }
    }
}

// Configure publishing
configure<org.ajoberstar.gradle.git.publish.GitPublishExtension> {
    val ghToken = System.getenv("GRGIT_USER")
    branch.set("gh-pages")
    commitMessage.set("Publish to GitHub Pages")
    contents.from(installGuides)
    if (ghToken != null) {
        repoUri.set("https://github.com/gradle/guides.git")
    } else {
        repoUri.set("git@github.com:gradle/guides.git")
    }
}

