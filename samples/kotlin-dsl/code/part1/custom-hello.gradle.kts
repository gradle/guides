tasks.create("hello") { // <1>
    doLast { // <2>
        println("Hello, World!")
    }
}
