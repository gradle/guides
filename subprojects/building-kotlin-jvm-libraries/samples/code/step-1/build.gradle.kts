// tag::apply-kotlin-plugin[]

plugins {
    kotlin("jvm") version "1.3.61" // <1>
}
// end::apply-kotlin-plugin[]

// tag::configure-dependencies[]
repositories {
    jcenter() // <2>
}

dependencies {
    implementation(kotlin("stdlib")) // <3>
}
// end::configure-dependencies[]
