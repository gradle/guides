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