tasks {
    create("hello") {
        group = "Welcome"
        description = "Produces a greeting"
        doLast {
            println("Hello, World")
        }
    }
}
