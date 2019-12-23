// tag::publishing-imports[]
// end::publishing-imports[]
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask

// tag::apply-maven-publish[]
plugins {
    `maven-publish` // <1>
// end::apply-maven-publish[]
    kotlin("jvm") version "1.3.61"
// tag::apply-maven-publish[]
    id("org.jetbrains.dokka") version "0.10.0"
}
// end::apply-maven-publish[]

// tag::project-coordinates[]
group = "org.example"
version = "0.0.1"
// end::project-coordinates[]

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:4.12")
}

// Configure existing Dokka task to output HTML to typical Javadoc directory
tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka) // <2>
}

// tag::configure-publishing[]
publishing {
    publications {
        create<MavenPublication>("default") { // <1>
            from(components["java"])
            artifact(dokkaJar) // <2>
        }
    }
    repositories {
        maven {
            url = uri("$buildDir/repository") // <3>
        }
    }
}
// end::configure-publishing[]
