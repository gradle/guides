plugins {
    java
}

task<Test>("integTest") {
}

// tag::distributionDirInput[]
class DistributionLocationProvider(                                     // <1>
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)                            // <2>
    var distribution: File
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> =
        listOf("-Ddistribution.location=${distribution.absolutePath}")  // <3>
}

// end::distributionDirInput[]


// tag::ignoreSystemProperties[]
class CiEnvironmentProvider : CommandLineArgumentProvider {
    @Internal                                                // <1>
    val agentNumber = System.getenv()["AGENT_NUMBER"] ?: "1"

    override fun asArguments(): Iterable<String> =
        listOf("-DagentNumber=$agentNumber")                 // <2>
}

// end::ignoreSystemProperties[]


// tag::integTest[]
// tag::distributionPathInput[]
// tag::distributionDirInput[]
// tag::ignoreSystemProperties[]
// tag::environment[]
tasks.getByName<Test>("integTest") {
// end::integTest[]
// end::distributionPathInput[]
// end::distributionDirInput[]
// end::ignoreSystemProperties[]
// end::environment[]


// tag::integTest[]
    inputs.property("operatingSystem") {
        System.getProperty("os.name")
    }
// end::integTest[]

// tag::distributionPathInput[]
    // Don't do this! Breaks relocatability!
    systemProperty("distribution.location", file("build/dist").absolutePath)
// end::distributionPathInput[]


// tag::distributionDirInput[]
    jvmArgumentProviders.add(
        DistributionLocationProvider(file("build/dist")) // <4>
    )
// end::distributionDirInput[]


// tag::ignoreSystemProperties[]
    jvmArgumentProviders.add(
        CiEnvironmentProvider()                              // <3>
    )
// end::ignoreSystemProperties[]


// tag::environment[]
    inputs.property("langEnvironment") {
        System.getenv("LANG")
    }
// end::environment[]


// tag::integTest[]
// tag::distributionPathInput[]
// tag::distributionDirInput[]
// tag::ignoreSystemProperties[]
// tag::environment[]
}
// end::integTest[]
// end::distributionPathInput[]
// end::distributionDirInput[]
// end::ignoreSystemProperties[]
// end::environment[]
