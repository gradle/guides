plugins {
    groovy
    `java-gradle-plugin`
    kotlin("jvm").version("1.2.71")
}

apply(from = "$rootDir/gradle/build-scan.gradle")
apply(from = "$rootDir/gradle/functional-test.gradle")
apply(from = "$rootDir/gradle/publishing-portal.gradle")
apply(from = "$rootDir/gradle/ghpages-sample.gradle")

val kotlinVersion by extra { "1.2.71" }

group = "com.github.gradle-guides"
version = "0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

repositories {
    jcenter()
}

dependencies {
    compile("org.freemarker:freemarker:2.3.26-incubating")
    testCompile("org.spockframework:spock-core:1.1-groovy-2.4") {
        exclude(group = "org.codehaus.groovy")
    }

    implementation(kotlin("stdlib-jdk8", kotlinVersion))
}

gradlePlugin {
    plugins {
        create("sitePlugin") {
            id = "com.github.gradle-guides.site"
            implementationClass = "org.gradle.plugins.site.SitePlugin"
        }
    }
}
