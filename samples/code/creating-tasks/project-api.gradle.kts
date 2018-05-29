// tag::project-api[]
task("greeting") {
    doLast { println("Hello, World!") }
}
// end::project-api[]

// tag::typed-project-api[]
task<Zip>("docZip") {
    archiveName = "doc.zip"
    from("doc")
}
// end::typed-project-api[]
