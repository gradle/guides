// tag::add-gretty[]
plugins {
    war
    id("org.gretty") version "2.2.0" // <1>
}
// end::add-gretty[]

repositories {
    jcenter()
}

// tag::add-mockito[]
dependencies {
    providedCompile("javax.servlet:javax.servlet-api:3.1.0")
    testCompile("junit:junit:4.12")
    testCompile("org.mockito:mockito-core:2.7.19")  // <1>
}
// end::add-mockito[]
