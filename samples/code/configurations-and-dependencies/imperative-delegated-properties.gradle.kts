apply(plugin = "java")
val implementation by configurations
val runtimeOnly by configurations
val testImplementation by configurations
val testRuntimeOnly by configurations
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.jsonwebtoken:jjwt:0.9.0")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
