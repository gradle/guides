plugins {
    java
}

// tag::timestamp[]
version = "3.2-${System.currentTimeMillis()}"

tasks.getByName<Jar>("jar") {
    manifest {
        attributes(mapOf("Implementation-Version" to version))
    }
}
// end::timestamp[]
