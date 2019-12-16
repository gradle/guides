import SitePlugin
import SiteExtension

apply<SitePlugin>()

configure<SiteExtension> {
    outputDir = file("build/mysite")

    customData {
        websiteUrl = "https://gradle.org"
        vcsUrl = "https://github.com/gradle-guides/gradle-site-plugin"
    }
}
