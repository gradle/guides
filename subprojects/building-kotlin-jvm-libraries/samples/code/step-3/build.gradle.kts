// tag::dokka-imports[]
import org.gradle.jvm.tasks.Jar
// end::dokka-imports[]

plugins {
    kotlin("jvm") version "1.3.61"
    // tag::apply-dokka-plugin[]
    id("org.jetbrains.dokka") version "0.10.0"
    // end::apply-dokka-plugin[]
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:4.12")
}

// tag::configure-dokka-plugin[]
tasks.dokka {    // <1>
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}
// end::configure-dokka-plugin[]

// tag::configure-dokka-jar[]
val dokkaJar by tasks.creating(Jar::class) { // <1>
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka) // <2>
}
// end::configure-dokka-jar[]
