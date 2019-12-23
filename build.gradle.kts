val guideProjects = extra["guideProjects"] as List<String>

tasks.register("clean") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

var buildTask = tasks.register("build") {
    dependsOn(gradle.includedBuilds.filter({ guideProjects.contains(it.name) }).map { it.task(":build") })
}

tasks.register("publishDocumentationPlugins") {
    dependsOn(gradle.includedBuild("gradle-guides-plugin").task(":publishPlugins"))
}

tasks.register("publishGuides") {
    dependsOn(buildTask)
    dependsOn(gradle.includedBuilds.filter({ guideProjects.contains(it.name) }).map { it.task(":gitPublishPush") })
}