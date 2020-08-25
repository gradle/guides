plugins {
    java
}

subprojects {
    apply(plugin = "java")
}

// tag::configureTask[]
tasks {
    val configureJar = create("configureJar") {
        doLast {
            jar.get().manifest {
                val classPath = listOf(":core", ":base-services").joinToString(" ") {
                    project(it).tasks.getByName<Jar>("jar").archivePath.name
                }
                attributes("Class-Path" to classPath)
            }
        }
    }
    jar { dependsOn(configureJar) }
}
// end::configureTask[]
