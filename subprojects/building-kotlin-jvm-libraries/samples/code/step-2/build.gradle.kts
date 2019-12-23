plugins {
    kotlin("jvm") version "1.3.61"
}

repositories {
    jcenter()
}

// tag::configure-dependencies[]
dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit:junit:4.12") // <1>
}
// tag::configure-dependencies[]
