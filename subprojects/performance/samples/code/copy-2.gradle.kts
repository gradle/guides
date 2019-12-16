plugins {
    java
}

// tag::copy[]
tasks.create<Copy>("copyFiles") {
    println(">> Compilation deps: ${configurations.compile.files}")
    into("$buildDir/output")
    from(configurations.compile)
}
// end::copy[]