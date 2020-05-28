// tag::init[]


plugins {
    `java-library` // <1>
}

repositories {
    jcenter() // <2>
}

dependencies {
    api("org.apache.commons:commons-math3:3.6.1") // <3>

    implementation("com.google.guava:guava:28.0-jre") // <4>

    testImplementation("junit:junit:4.12") // <5>
}

// end::init[]

// tag::version[]
version = "0.1.0"
// end::version[]

// tag::javadoc[]
java {
    withJavadocJar()
}
// end::javadoc[]

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

