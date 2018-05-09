plugins {
    java
}

// tag::trackVendor[]
tasks.withType<AbstractCompile> {
    inputs.property("java.vendor") {
        System.getProperty("java.vendor")
    }
}

tasks.withType<Test> {
    inputs.property("java.vendor") {
        System.getProperty("java.vendor")
    }
}
// end::trackVendor[]
