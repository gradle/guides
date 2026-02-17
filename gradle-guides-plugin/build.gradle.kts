plugins {
    id("groovy")
    id("java-gradle-plugin")
    alias(libs.plugins.publish)
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
    api(libs.asciidoctor.jvm.convert)
    implementation(libs.ant)
    implementation(libs.jsoup)

    // For exemplar asciidoctor tests
    compileOnly(libs.commons.io)
    compileOnly(libs.commons.lang3)
    compileOnly(libs.asciidoctorj)
    compileOnly(libs.exemplar.samples.check)
    compileOnly(libs.tapi)

    implementation(libs.junit4)
    implementation(libs.ansi.control.sequence.util) // For rich and verbose console support
    implementation(libs.asciidoctor.gradle.base)
    implementation(libs.asciidoctorj.api)
    implementation(libs.exemplar.samples.discovery)

    testImplementation(localGroovy())
    testImplementation(libs.spock.core)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.platform.launcher)
}

gradlePlugin {
    testSourceSets += sourceSets["functionalTest"]
    website.set("https://guides.gradle.org")
    vcsUrl.set("https://github.com/gradle/guides")
    plugins {
        create("guidePlugin") {
            id = "org.gradle.guide"
            implementationClass = "org.gradle.docs.guides.internal.LegacyGuideDocumentationPlugin"
            displayName = "Gradle Guides Plugin"
            description = "Plugin for authoring Gradle guides"
            tags.set(listOf("gradle", "guides", "documentation"))
        }
        create("samplePlugin") {
            id = "org.gradle.samples"
            implementationClass = "org.gradle.docs.samples.internal.LegacySamplesDocumentationPlugin"
            displayName = "Gradle Sample Plugin"
            description = "Plugin required for authoring new Gradle Samples"
            tags.set(listOf("gradle", "guides", "documentation"))
        }
        create("documentationPlugin") {
            id = "org.gradle.documentation"
            implementationClass = "org.gradle.docs.internal.BuildDocumentationPlugin"
            displayName = "Gradle Documentation Plugin"
            description = "Plugin for authoring Gradle documentation"
            tags.set(listOf("gradle", "guides", "documentation"))
        }
    }
}

tasks.named("publishPlugins") {
    onlyIf { !"$version".endsWith("-SNAPSHOT") }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
