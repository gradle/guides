// tag::reference[]
// greeting is of type Task
val greeting by tasks.creating {
    doLast { println("Hello, World!") }
}
// end::reference[]

// tag::typed-reference[]
// docZip is of type Zip
val docZip by tasks.creating(Zip::class) {
    archiveName = "doc.zip"
    from("doc")
}
// end::typed-reference[]
