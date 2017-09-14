// tag::publishing-imports[]
// end::publishing-imports[]
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask

// Use of buildscript {} necessary due to https://github.com/Kotlin/dokka/issues/146
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.15")
    }
}
apply {
    plugin("org.jetbrains.dokka")
}

// tag::apply-maven-publish[]
plugins {
// end::apply-maven-publish[]
    `build-scan`
// tag::apply-maven-publish[]
    `maven-publish` // <1>
// end::apply-maven-publish[]
    kotlin("jvm", "1.1.4-3")
// tag::apply-maven-publish[]
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
    implementation(kotlin("stdlib", "1.1.4-3"))
    testImplementation("junit:junit:4.12")
}

buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")

    publishAlways()
}

// Configure existing Dokka task to output HTML to typical Javadoc directory
val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(dokka) // <2>
}

// tag::configure-publishing[]
publishing {
    publications {
        create("default", MavenPublication::class.java) { // <1>
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
