// tag::dokka-imports[]
import org.gradle.jvm.tasks.Jar
// end::dokka-imports[]

plugins {
    `build-scan`
    kotlin("jvm") version "1.2.31"
    // tag::apply-dokka-plugin[]
    id("org.jetbrains.dokka") version "0.9.16"
    // end::apply-dokka-plugin[]
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib", "1.2.31"))
    testImplementation("junit:junit:4.12")
}

buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")

    publishAlways()
}

// tag::configure-dokka-plugin[]
val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {    // <1>
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}
// end::configure-dokka-plugin[]

// tag::configure-dokka-jar[]
val dokkaJar by tasks.creating(Jar::class) { // <1>
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(dokka) // <2>
}
// end::configure-dokka-jar[]
