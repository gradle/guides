// tag::use-plugin-class-name-with-info[]
apply {
  plugin(org.example.greeting.GreetingPlugin::class.java) // <1>
}
// end::use-plugin-class-name-with-info[]

// tag::use-plugin-class-name[]
apply {
  plugin(org.example.greeting.GreetingPlugin::class.java)
}
// end::use-plugin-class-name[]
