open class Greeting: DefaultTask() { // <1> <2>
    lateinit var message: String   // <3>
    lateinit var recipient: String

    @TaskAction // <4>
    fun sayGreeting() {
        println("$message, $recipient!") // <5>
    }
}

tasks.create<Greeting>("hello") { // <6>
    group = "Welcome"
    description = "Produces a world greeting"
    message = "Hello" // <7>
    recipient = "World"
}
