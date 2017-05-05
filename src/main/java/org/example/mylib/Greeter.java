// tag::code[]
package org.example.mylib;
// end::code[]

// tag::javadoc[]
/** A class that will produce a greeting for a provided name.
 *
 */
// tag::code[]
public class Greeter {
    private String name;

    public Greeter(String name) { this.name = name; }

    public String getGreeting() { return "Hello, " + this.name + "!"; }
}
// end::code[]
// end::javadoc[]
