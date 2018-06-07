// tag::reference[]
val greeting by tasks.creating {
    doLast { println("Hello, World!") }
}
// end::reference[]

// tag::typed-reference[]
val docZip by tasks.creating(Zip::class) {
    archiveName = "doc.zip"
    from("doc")
}
// end::typed-reference[]
