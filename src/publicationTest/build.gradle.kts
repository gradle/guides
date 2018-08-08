// tag::plugins_block[]
plugins {
    `java-gradle-plugin` // <1>
    id("com.gradle.plugin-publish") version "0.10.0"  // <2>
}
// end::plugins_block[]

// tag::gradle-plugin[]
gradlePlugin {
    plugins { // <1>
        create("greetingsPlugin") { // <2>
            id = "<your plugin identifier>" // <3>
            implementationClass = "<your plugin class>"
        }
    }
}
// end::gradle-plugin[]

// tag::plugin_bundle[]
pluginBundle {
// tag::global_information[]
    website = "<substitute your project website>" // <1>
    vcsUrl = "<uri to project source repository>" // <2>
    tags = listOf("default", "tags", "unless", "overridden", "in", "plugin") // <3>
// end::global_information[]
// tag::plugin_definitions[]
    plugins { // <1>
        create("greetingsPlugin") { // <2>
            // id is captured from gradlePlugin extension block // <3>
            displayName = "<short displayable name for plugin>" // <4>
            description = "<Good human-readable description of what your plugin is about>" // <5>
            tags = listOf("individual", "tags", "per", "plugin") // <6>
            version = "1.2" // <7>
        }
    }
// end::plugin_definitions[]
}
// end::plugin_bundle[]
