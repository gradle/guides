plugins {
    jacoco
}
apply(plugin = "checkstyle")

jacoco {
    toolVersion = "0.8.1"
}

configure<CheckstyleExtension> {
    maxErrors = 10
}
