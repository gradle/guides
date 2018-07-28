open class Greeting: DefaultTask() { // <1> <2>
    var message: String? = null // <3>
    var recipient: String? = null

    @TaskAction // <4>
    fun sayGreeting() = println("${message}, ${recipient}!") // <5>
}

tasks {
    create<Greeting>("hello") { // <6>
        group = "Welcome"
        description = "Produces a world greeting"
        message = "Hello" // <7>
        recipient = "World"
    }
}
