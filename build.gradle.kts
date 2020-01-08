plugins {
    id("org.ajoberstar.git-publish") version("2.1.3")
}

val guideProjects = extra["guideProjects"] as List<String>

tasks.register("clean") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

var buildTask = tasks.register("build") {
    dependsOn(guideProjects.map { ":${it}:build" })
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
    }
}
dependencies {
    guideProjects.forEach {
        add(guides.name, project(":${it}"))
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

// Test JVM codes
allprojects {
    pluginManager.withPlugin("org.gradle.guides") {
        repositories {
            maven {
                url = uri("https://repo.gradle.org/gradle/libs-releases")
            }
        }

        dependencies {
            add("docsTestImplementation", gradleTestKit())
            add("docsTestImplementation", "org.spockframework:spock-core:1.2-groovy-2.5")
            add("docsTestImplementation", "commons-io:commons-io:2.5")
            add("docsTestImplementation", "org.gradle.guides:test-fixtures:0.4")
        }

        // Passes the system property {@literal "samplesDir"} with the value {@literal "$projectDir/samples"} to {@code "test"} task.
        tasks.named("docsTest", Test::class.java) {
            val samplesBaseDir = project.file("samples")
            inputs.files(samplesBaseDir).withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional()
            // This breaks relocatability of the test task. If caching becomes important we should consider redefining the inputs for the test task
            systemProperty("samplesDir", samplesBaseDir.absolutePath)
        }
    }
}

// Attach checkGuides to check
allprojects {
    pluginManager.withPlugin("org.gradle.guides") {
        tasks.named("check") { dependsOn("checkGuides") }
    }
}