import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.asciidoctor.convert")       // <1>
}

tasks.asciidoctor {
    sources(delegateClosureOf<PatternSet> {
        include("greeter.adoc")         // <2>
    })
}

tasks.build { dependsOn(tasks.asciidoctor) }

