plugins {
    java
}

// tag::namespace[]
tasks.jar {
    archiveName = "foo.jar"
}
// end::namespace[]

// tag::using-api[]
tasks.named<Jar>("jar") {
    archiveName = "foo.jar"
}
// end::using-api[]

// tag::using-eager-api[]
tasks.getByName<Jar>("jar") {
    archiveName = "foo.jar"
}
// end::using-eager-api[]
