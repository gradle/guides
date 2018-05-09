allprojects {
    apply(plugin = "java")
}

// tag::customAction[]
tasks.getByName<Jar>("jar") {
    doFirst {
        manifest {
            val classPath = listOf(":core", ":baseServices").joinToString(" ") {
                (project(it).tasks["jar"] as Jar).archivePath.name
            }
            attributes(mapOf("Class-Path" to classPath))
        }
    }
}
// end::customAction[]
