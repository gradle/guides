import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.internal.impldep.org.junit.platform.launcher.EngineFilter.includeEngines
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.serialization.js.DynamicTypeDeserializer.id

plugins {
    `build-scan`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish").version("0.10.0")
    kotlin("jvm").version("1.3.10")
}

val kotlinVersion by extra { "1.3.10" }
val junitPlatformVersion by extra { "1.1.0" }
val spekVersion by extra { "2.0.0-rc.1" }

group = "org.gradle.plugins"
version = "0.2"

val websiteUrl by extra { "https://gradle-guides.github.io/${project.name}/" }
val githubUrl by extra { "https://github.com/gradle-guides/${project.name}.git" }

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output + configurations.testRuntime.get()
        runtimeClasspath += output + compileClasspath
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val intTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.freemarker:freemarker:2.3.26-incubating")

    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    testImplementation(kotlin("test", kotlinVersion))
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }

    testRuntimeOnly(kotlin("reflect", kotlinVersion))
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion") {
        exclude(group = "org.junit.platform")
        exclude(group = "org.jetbrains.kotlin")
    }

    testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")

    intTestImplementation("org.jsoup:jsoup:1.10.2")
    intTestImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("sitePlugin") {
            id = "gradle.site"
            implementationClass = "org.gradle.plugins.site.SitePlugin"
        }
    }
}

pluginBundle {
    website = websiteUrl
    vcsUrl = githubUrl
    tags = listOf("documentation", "site")
    description = "Generates documentation in HTML for given project"

    (plugins) {
        "sitePlugin" {
            displayName = "Gradle Site Plugin"
        }
    }
}

tasks {
    create<Test>("integrationTest") {
        description = "Runs the functional tests"
        group = JavaBasePlugin.VERIFICATION_GROUP

        testClassesDirs = sourceSets["intTest"].output.classesDirs
        classpath = sourceSets["intTest"].runtimeClasspath
        shouldRunAfter(test)

        reports {
            html.destination = project.file("${html.destination}/functional")
            junitXml.destination = project.file("${junitXml.destination}/functional")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    check {
        dependsOn("integrationTest")
    }
}
