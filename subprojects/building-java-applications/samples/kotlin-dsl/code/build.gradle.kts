plugins {
    application // <1>
}

repositories {
    jcenter() // <2>
}

dependencies {
    implementation("com.google.guava:guava:26.0-jre") // <3>

    testImplementation("junit:junit:4.12") // <4>
}

application {
    mainClassName = "demo.App" // <5>
}
