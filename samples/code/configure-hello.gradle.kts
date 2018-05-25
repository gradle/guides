// tag::configure-hello[]
import org.example.greeting.Greeting

// end::configure-hello[]

// tag::use-plugin-id[]
apply {
  plugin("org.example.greeting")
}
// end::use-plugin-id[]

// tag::configure-hello[]
tasks.getByName("hello", closureOf<Greeting> { // <1>
    message = "Hi"
    recipient = "Gradle"
})
// end::configure-hello[]
