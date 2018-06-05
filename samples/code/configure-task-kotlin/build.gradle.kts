allprojects {
    apply(plugin = "java")
}

// tag::configureTask[]
tasks {
    val jar = getByName<Jar>("jar")
    val configureJar = create("configureJar") {
        doLast {
            jar.manifest {
                val classPath = listOf(":core", ":baseServices").joinToString(" ") {
                    (project(it).tasks["jar"] as Jar).archivePath.name
                }
                attributes(mapOf("Class-Path" to classPath))
            }
        }
    }
    jar.dependsOn(configureJar)
}
// end::configureTask[]
