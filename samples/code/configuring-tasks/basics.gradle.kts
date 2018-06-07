plugins {
    java
}

// tag::single[]
tasks.getByName<Jar>("jar").archiveName = "foo.jar"
// end::single[]

// tag::untyped[]
tasks["test"].doLast {
    println("test completed")
}
// end::untyped[]

// tag::config[]
tasks.getByName<Jar>("jar") {
    archiveName = "foo.jar"
    into("META-INF") {
        from("bar")
    }
}
// end::config[]

// tag::reference[]
val jar by tasks.getting(Jar::class) {
    archiveName = "foo.jar"
}

jar.into("META-INF") {
    from("bar")
}
// end::reference[]
