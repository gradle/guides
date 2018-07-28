open class Greeting: DefaultTask() {
    var message: String? = null
    var recipient: String? = null

    @TaskAction
    fun sayGreeting() = println("${message}, ${recipient}!")
}

// tag::german[]
tasks {
// end::german[]
    create<Greeting>("hello") {
        group = "Welcome"
        description = "Produces a world greeting"
        message = "Hello"
        recipient = "World"
    }
// tag::german[]
    // [...]
    create<Greeting>("gutenTag") {
        group = "Welcome"
        description = "Produces a German greeting"
        message = "Guten Tag"
        recipient = "Welt"
    }
}
// end::german[]
