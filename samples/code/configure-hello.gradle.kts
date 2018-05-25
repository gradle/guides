// tag::configure-hello[]
import org.example.greeting.Greeting

// end::configure-hello[]

// tag::use-plugin-id[]
plugins {
  id("org.example.greeting")
}
// end::use-plugin-id[]

// tag::configure-hello[]
tasks.getByName<Greeting>("hello") { // <1>
    message = "Hi"
    recipient = "Gradle"
}
// end::configure-hello[]
