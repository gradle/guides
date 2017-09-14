// tag::dokka-imports[]
import org.gradle.jvm.tasks.Jar
// end::dokka-imports[]

// tag::apply-dokka-plugin[]
// Using buildscript {} instead of plugins {} due to https://github.com/Kotlin/dokka/issues/146
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
// end::apply-dokka-plugin[]

plugins {
    `build-scan`
    kotlin("jvm", "1.1.4-3")
}

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
