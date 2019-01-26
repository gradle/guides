// tag::build-scan-plugin-plugins-apply[]
plugins {
    id("com.gradle.build-scan") version "2.1" // <1>
}
// end::build-scan-plugin-plugins-apply[]

// tag::build-scan-dsl[]
buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
// end::build-scan-dsl[]

apply(plugin = "base")
