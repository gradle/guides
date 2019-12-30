// tag::init[]


plugins {
    `java-library`
}

repositories {
    jcenter() // <1>
}

dependencies {
    api("org.apache.commons:commons-math3:3.6.1") // <2>

    implementation("com.google.guava:guava:28.0-jre") // <3>

    testImplementation("junit:junit:4.12") // <4>
}

// end::init[]

// tag::version[]
version = "0.1.0"
// end::version[]

// tag::jar-manifest[]
tasks {
    jar {
        manifest {
            attributes(
                mapOf("Implementation-Title" to project.name,
                      "Implementation-Version" to project.version)
            )
        }
    }
}
// end::jar-manifest[]

