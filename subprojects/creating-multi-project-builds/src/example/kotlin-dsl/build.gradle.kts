// tag::plugins[]
plugins {
    id("org.asciidoctor.convert") version "1.5.6" apply false // <1>
}
// end::plugins[]

// tag::jcenter[]
allprojects {
    repositories {
        jcenter() // <1>
    }
}
// end::jcenter[]

// tag::subproject-versions[]
subprojects {
    version = "1.0"
}
// end::subproject-versions[]

// tag::common-code-1[]
configure(subprojects.filter { it.name == "greeter" || it.name == "greeting-library" }) { // <1>

    apply(plugin = "groovy")
// end::common-code-1[]
    apply(from = "${rootProject.projectDir}/spock.gradle.kts")
// tag::common-code-2[]
}
// end::common-code-2[]
