// tag::reproducibleZip[]
tasks.create<Zip>("createZip") {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    // ...
}
// end::reproducibleZip[]
