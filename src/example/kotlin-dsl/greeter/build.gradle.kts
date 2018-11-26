// tag::basic-script[]
plugins {
    java        // <1>
    application // <2>
}
// end::basic-script[]

// tag::main-class-name[]
application {
    mainClassName = "greeter.Greeter" // <1>
}
// end::main-class-name[]

// tag::project-dependency[]
dependencies {
    compile(project(":greeting-library")) // <1>
}
// end::project-dependency[]

// tag::link-docs[]
tasks.distZip {
    from(project(":docs").tasks["asciidoctor"]) { // <1>
        into("${project.name}-$version")
    }
}
tasks.distTar {
    from(project(":docs").tasks["asciidoctor"]) {
        into("${project.name}-$version")
    }
}
// end::link-docs[]
