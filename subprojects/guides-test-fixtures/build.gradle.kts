plugins {
    groovy
}

apply(from = "$rootDir/gradle/publishing.gradle")

group = "org.gradle.guides"
version = "0.4"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleTestKit())
    implementation("org.spockframework:spock-core:1.1-groovy-2.4") {
        exclude(module = "groovy-all")
    }
    testImplementation(localGroovy())
}
