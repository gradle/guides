// tag::apply-kotlin-plugin[]

// tag::apply-build-scan-plugin[]
plugins {
// end::apply-kotlin-plugin[]
    `build-scan`
// tag::apply-kotlin-plugin[]
    kotlin("jvm") version "1.2.71" // <1>
}
// end::apply-kotlin-plugin[]
// end::apply-build-scan-plugin[]

// tag::configure-dependencies[]
repositories {
    jcenter() // <2>
}

dependencies {
    implementation(kotlin("stdlib")) // <3>
}
// end::configure-dependencies[]

// tag::configure-build-scan[]
buildScan {
    setLicenseAgreementUrl("https://gradle.com/terms-of-service") // <1>
    setLicenseAgree("yes")

    publishAlways() // <2>
}
// end::configure-build-scan[]
