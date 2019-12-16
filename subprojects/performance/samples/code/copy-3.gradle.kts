plugins {
    java
}
// tag::copy[]
tasks.create<Copy>("copyFiles") {
    into("$buildDir/output")
    from(configurations.compile)
    doFirst {
        println(">> Compilation deps: ${configurations.compile.files}")
    }
}
// end::copy[]