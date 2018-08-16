// tag::delegated-properties[]
apply(plugin = "java")
val implementation by configurations
val runtimeOnly by configurations
val testImplementation by configurations
val testRuntimeOnly by configurations
dependencies {
    implementation("com.example:lib:1.1")
    runtimeOnly("com.example:runtime:1.0")
    testImplementation("com.example:test-support:1.3") {
        exclude(module = "junit")
    }
    testRuntimeOnly("com.example:test-junit-jupiter-runtime:1.3")
}
// end::delegated-properties[]

// tag::string-invoke[]
apply(plugin = "java")
dependencies {
    "implementation"("com.example:lib:1.1")
    "runtimeOnly"("com.example:runtime:1.0")
    "testImplementation"("com.example:test-support:1.3") {
        exclude(module = "junit")
    }
    "testRuntimeOnly"("com.example:test-junit-jupiter-runtime:1.3")
}
// end::string-invoke[]
