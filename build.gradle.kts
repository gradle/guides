val guideAsCompositeProjects = extra["guideAsCompositeProjects"] as List<String>
val guideAsProjects = extra["guideAsProjects"] as List<String>

tasks.register("clean") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

var buildTask = tasks.register("build") {
    dependsOn(gradle.includedBuilds.filter({ guideAsCompositeProjects.contains(it.name) }).map { it.task(":build") })
    dependsOn(guideAsProjects.map { ":${it}:build" })
}

tasks.register("publishDocumentationPlugins") {
    dependsOn(gradle.includedBuild("gradle-guides-plugin").task(":publishPlugins"))
}

tasks.register("publishGuides") {
    // TODO: Introduce instead a publishGuides task within each project to avoid this dependency
    dependsOn(buildTask)
    dependsOn(guideAsProjects.map { ":${it}:gitPublishPush" })
    dependsOn(gradle.includedBuilds.filter({ guideAsCompositeProjects.contains(it.name) }).map { it.task(":gitPublishPush") })
}