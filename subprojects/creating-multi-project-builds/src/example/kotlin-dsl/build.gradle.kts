// tag::plugins-with-build-scan[]
// tag::plugins[]
plugins {
    // end::plugins[]
    id("com.gradle.build-scan") version "2.0.2"
// tag::plugins[]
    id("org.asciidoctor.convert") version "1.5.6" apply false // <1>
}
// end::plugins[]

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
// end::plugins-with-build-scan[]

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
