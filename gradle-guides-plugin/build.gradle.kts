plugins {
    id("groovy")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish").version("0.20.0")
    id("maven-publish")
}

sourceSets {
    val functionalTest by creating {
        groovy.srcDirs("src/functionalTest/groovy")
        resources.srcDirs("src/functionalTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests."
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    maxParallelForks = 2
    systemProperty("gradle.version", gradle.gradleVersion)
}

tasks.check {
    dependsOn(tasks.getByName("functionalTest"))
}

group = "org.gradle.guides"
version = "0.24.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    maven {
        name = "Gradle public repository"
        url = uri("https://repo.gradle.org/gradle/public")
        content {
            includeModule("org.gradle", "gradle-tooling-api")
            includeModuleByRegex("org.gradle", "sample-(check|discovery)")
        }
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api("org.asciidoctor:asciidoctor-gradle-jvm:4.0.1")
    implementation("org.apache.ant:ant:1.10.15")
    implementation("org.jsoup:jsoup:1.20.1")

    // For exemplar asciidoctor tests
    compileOnly("commons-io:commons-io:2.14.0")
    compileOnly("org.apache.commons:commons-lang3:3.9")
    compileOnly("org.asciidoctor:asciidoctorj:2.4.3")
    compileOnly("org.gradle.exemplar:samples-check:1.0.3")
    compileOnly("org.gradle:gradle-tooling-api:6.0.1")

    implementation("junit:junit:4.13.2")
    implementation("net.rubygrapefruit:ansi-control-sequence-util:0.3") // For rich and verbose console support
    implementation("org.asciidoctor:asciidoctor-gradle-base:4.0.1")
    implementation("org.asciidoctor:asciidoctorj-api:2.4.3")
    implementation("org.gradle.exemplar:samples-discovery:1.0.3")

    testImplementation("org.codehaus.groovy:groovy:3.0.24")
    testImplementation("org.spockframework:spock-core:2.1-groovy-3.0") {
        exclude(module = "groovy-all")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
}

pluginBundle {
    website = "https://guides.gradle.org"
    vcsUrl = "https://github.com/gradle/guides"
    tags = listOf("gradle", "guides", "documentation")
}

gradlePlugin {
    testSourceSets += sourceSets["functionalTest"]
    plugins {
        create("guidePlugin") {
            id = "org.gradle.guide"
            implementationClass = "org.gradle.docs.guides.internal.LegacyGuideDocumentationPlugin"
            displayName = "Gradle Guides Plugin"
            description = "Plugin for authoring Gradle guides"
        }
        create("samplePlugin") {
            id = "org.gradle.samples"
            implementationClass = "org.gradle.docs.samples.internal.LegacySamplesDocumentationPlugin"
            displayName = "Gradle Sample Plugin"
            description = "Plugin required for authoring new Gradle Samples"
        }
        create("documentationPlugin") {
            id = "org.gradle.documentation"
            implementationClass = "org.gradle.docs.internal.BuildDocumentationPlugin"
            displayName = "Gradle Documentation Plugin"
            description = "Plugin for authoring Gradle documentation"
        }
    }
}

tasks.named("publishPlugins") {
    onlyIf { !"$version".endsWith("-SNAPSHOT") }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
