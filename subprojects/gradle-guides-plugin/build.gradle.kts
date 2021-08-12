plugins {
    id("groovy")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.15.0"
    id("maven-publish")
}

apply(from = "$rootDir/gradle/functional-test.gradle")

group = "org.gradle.guides"
version = "0.18.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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
    implementation("net.sourceforge.nekohtml:nekohtml:1.9.21")
    implementation("org.codehaus.groovy.modules:http-builder-ng-core:0.11.1")

    implementation("org.asciidoctor:asciidoctor-gradle-jvm:3.3.0")
    implementation("org.apache.ant:ant:1.9.13")

    // For exemplar asciidoctor tests
    compileOnly("org.gradle:gradle-tooling-api:6.0.1")
    compileOnly("org.apache.commons:commons-lang3:3.9")
    compileOnly("org.asciidoctor:asciidoctorj:2.4.2")
    compileOnly("org.gradle:sample-check:0.12.6")
    compileOnly("commons-io:commons-io:2.6")

    // For rich and verbose console support
    implementation("net.rubygrapefruit:ansi-control-sequence-util:0.3")

    testImplementation("org.spockframework:spock-core:1.2-groovy-2.5") {
        exclude(module = "groovy-all")
    }
    testImplementation("org.yaml:snakeyaml:1.21")
}

pluginBundle {
    website = "https://guides.gradle.org"
    vcsUrl = "https://github.com/gradle/guides"
    tags = listOf("gradle", "guides", "documentation")
}

gradlePlugin {
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

tasks {
    val publishPlugins by getting {
        dependsOn("build")

        onlyIf { !"$version".endsWith("-SNAPSHOT")}
    }

    register("release") {
        dependsOn("build", publishPlugins)
    }
}