import java.util.Properties

plugins {
    java
}

version = "3.2-${System.currentTimeMillis()}"

// tag::normalization[]
normalization {
    runtimeClasspath {
        ignore("build-info.properties")
    }
}
// end::normalization[]

val generatedResourcesDir = file("$buildDir/generated-resources")
// tag::versionInfo[]

val currentVersionInfo = tasks.create<CurrentVersionInfo>("currentVersionInfo") {
    version = project.version.toString()
    versionInfoFile = File(generatedResourcesDir, "currentVersion.properties")
}

sourceSets.main { output.dir(generatedResourcesDir, "builtBy" to currentVersionInfo) }

open class CurrentVersionInfo : DefaultTask() {
    @Input
    lateinit var version: String

    @OutputFile
    lateinit var versionInfoFile: File

    @TaskAction
    fun writeVersionInfo() {
        val properties = Properties()
        properties["latestMilestone"] = version
        versionInfoFile.outputStream().use { out ->
            properties.store(out, null)
        }
    }
}
// end::versionInfo[]
