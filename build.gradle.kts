val guideProjects = extra["guideProjects"] as List<String>

println(gradle.gradleVersion)
tasks.register("build") {
    dependsOn(gradle.includedBuilds.filter({ guideProjects.contains(it.name) }).map { it.task(":build") })
}
