plugins {
    java
}

// tag::conditionalAction[]
if ("CI" in System.getenv()) {
    tasks.withType<Test> {
        doFirst {
            println("Running test on CI")
        }
    }
}
// end::conditionalAction[]

// tag::unconditionalAction[]
tasks.withType<Test> {
    doFirst {
        if ("CI" in System.getenv()) {
            println("Running test on CI")
        }
    }
}
// end::unconditionalAction[]
