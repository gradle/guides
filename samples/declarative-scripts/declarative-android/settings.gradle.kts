// tag::repo[]
// tag::plugin[]
pluginManagement {
// end::plugin[]
    repositories {
        gradlePluginPortal()
        google()
    }
// end::repo[]
// tag::plugin[]
    // ...
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
// tag::repo[]
}
// end::repo[]
// end::plugin[]
