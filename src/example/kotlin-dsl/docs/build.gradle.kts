import org.asciidoctor.gradle.AsciidoctorTask

plugins {
    id("org.asciidoctor.convert")       // <1>
}

val asciidoctor = tasks.getByName<AsciidoctorTask>("asciidoctor") {
    sources(delegateClosureOf<PatternSet> {
        include("greeter.adoc")         // <2>
    })
}

tasks["build"].dependsOn(asciidoctor)
