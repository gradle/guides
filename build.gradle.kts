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

tasks.register("publishGuides") {
    // TODO: Introduce instead a publishGuides task within each project to avoid this dependency
    dependsOn(buildTask)
    dependsOn(guideProjects.map { ":${it}:gitPublishPush" })
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
    into("$buildDir/published-guides")
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