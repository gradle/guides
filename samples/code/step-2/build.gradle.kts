import org.gradle.kotlin.dsl.*

plugins {
    `build-scan`
    kotlin("jvm", "1.1.4-3")
}

repositories {
    jcenter()
}

// tag::configure-dependencies[]
dependencies {
    implementation(kotlin("stdlib", "1.1.4-3"))
    testImplementation("junit:junit:4.12") // <1>
}
// tag::configure-dependencies[]

buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service")
    setLicenseAgree("yes")

    publishAlways()
}
