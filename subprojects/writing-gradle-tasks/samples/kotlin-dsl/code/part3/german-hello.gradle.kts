open class Greeting: DefaultTask() {
    lateinit var message: String
    lateinit var recipient: String

    @TaskAction
    fun sayGreeting() {
        println("$message, $recipient!")
    }
}

tasks.register<Greeting>("hello") {
    group = "Welcome"
    description = "Produces a world greeting"
    message = "Hello"
    recipient = "World"
}

// tag::german[]
tasks.register<Greeting>("gutenTag") {
    group = "Welcome"
    description = "Produces a German greeting"
    message = "Guten Tag"
    recipient = "Welt"
}
// end::german[]
