// tag::plugins[]
plugins {
    id("base")
}
// end::plugins[]

version = "1.0"

// tag::zip[]
tasks.create<Zip>("zip") {
    description = "Archives sources in a zip file"
    group = "Archive"

    from("src")
    setArchiveName("basic-demo-1.0.zip")
}
// end::zip[]
