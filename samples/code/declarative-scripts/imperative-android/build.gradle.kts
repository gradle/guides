import com.android.build.gradle.AppExtension

buildscript {                               // <1>
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.1.0")
    }
}

apply(plugin = "com.android.application")   // <2>

configure<AppExtension> {                   // <3>
    buildToolsVersion("27.0.3")
    compileSdkVersion(27)
}
