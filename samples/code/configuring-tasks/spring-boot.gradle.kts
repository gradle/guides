// tag::lazy[]
// tag::eager[]
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "2.0.2.RELEASE"
}

// end::lazy[]
// end::eager[]

// tag::lazy[]
tasks {
    named<BootJar>("bootJar") {
        archiveName = "app.jar"
        mainClassName = "com.example.demo.Demo"
    }

    named<BootRun>("bootRun") {
        main = "com.example.demo.Demo"
        args("--spring.profiles.active=demo")
    }
}
// end::lazy[]

// tag::eager[]
tasks {
    getByName<BootJar>("bootJar") {
        archiveName = "app.jar"
        mainClassName = "com.example.demo.Demo"
    }

    getByName<BootRun>("bootRun") {
        main = "com.example.demo.Demo"
        args("--spring.profiles.active=demo")
    }
}
// end::eager[]
