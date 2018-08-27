// tag::reference[]
// greeting is of type TaskProvider<Task>
val greeting by tasks.registering {
    doLast { println("Hello, World!") }
}
// end::reference[]

// tag::typed-reference[]
// docZip is of type TaskProvider<Zip>
val docZip by tasks.registering(Zip::class) {
    archiveName = "doc.zip"
    from("doc")
}
// end::typed-reference[]
