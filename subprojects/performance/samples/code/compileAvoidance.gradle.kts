plugins {
    java
}

// tag::incremental[]
tasks.withType<JavaCompile> {
    options.isIncremental = true
}
// end::incremental[]
