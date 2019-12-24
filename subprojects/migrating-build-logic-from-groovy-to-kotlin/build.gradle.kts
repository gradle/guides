plugins {
    id("org.gradle.guides")
    id("org.gradle.guides.test-jvm-code")
}

guide {
    repositoryPath.set("gradle-guides/migrating-build-logic-from-groovy-to-kotlin")
    minimumGradleVersion.set("5.0")
    title.set("Migrating build logic from Groovy to Kotlin")
    category.set("Topical")
}

tasks.test {
    inputs.property("androidHome", System.getenv("ANDROID_HOME") ?: "")
}

repositories {
    maven(url = "https://repo.gradle.org/gradle/libs")
}

dependencies {
    constraints {
        testImplementation("org.codehaus.groovy:groovy-all:2.5.4")
    }
    testImplementation("org.gradle:sample-check:0.6.0")
    testImplementation(gradleTestKit())
}
