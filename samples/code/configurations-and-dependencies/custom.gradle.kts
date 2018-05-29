plugins {
    java
}

// tag::delegated-properties[]
val db by configurations.creating
val integTestImplementation by configurations.creating {
    extendsFrom(configurations["testImplementation"])
}

dependencies {
    db("org.postgresql:postgresql")
    integTestImplementation("com.ninja-squad:DbSetup:2.1.0")
}
// end::delegated-properties[]

// tag::string-reference[]
// get the existing testRuntimeOnly configuration
val testRuntimeOnly by configurations

dependencies {
    testRuntimeOnly("org.postgresql:postgresql")
    "db"("org.postgresql:postgresql")
    "integTestImplementation"("com.ninja-squad:DbSetup:2.1.0")
}
// end::string-reference[]
