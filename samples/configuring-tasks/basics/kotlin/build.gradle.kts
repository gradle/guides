plugins {
    java
}

// tag::single-eager[]
tasks.getByName<Jar>("jar").archiveName = "foo.jar"
// end::single-eager[]

// tag::single-lazy[]
tasks.named<Jar>("jar") {
    archiveName = "foo.jar"
}
// end::single-lazy[]

// tag::untyped-lazy[]
tasks.named<Task>("test") {
    doLast {
        println("test completed")
    }
}
// end::untyped-lazy[]

// tag::untyped-eager[]
tasks.getByName("test").doLast {
    println("test completed")
}
// end::untyped-eager[]


// tag::config-lazy[]
tasks.named<Jar>("jar") {
    archiveName = "foo.jar"
    into("META-INF") {
        from("bar")
    }
}
// end::config-lazy[]

// tag::config-eager[]
tasks.getByName<Jar>("jar") {
    archiveName = "foo.jar"
    into("META-INF") {
        from("bar")
    }
}
// end::config-eager[]


run {

// tag::reference-lazy[]
val jar by tasks.existing(Jar::class) {
    archiveName = "foo.jar"
}

jar {
    into("META-INF") {
        from("bar")
    }
}
// end::reference-lazy[]

}

run {

// tag::reference-eager[]
val jar by tasks.getting(Jar::class) {
    archiveName = "foo.jar"
}

jar.into("META-INF") {
    from("bar")
}
// end::reference-eager[]

}
