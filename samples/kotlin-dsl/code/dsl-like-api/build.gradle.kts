import SitePlugin
import SiteExtension

apply<SitePlugin>()

configure<SiteExtension> {
    outputDir = file("build/mysite")

    customData {
        websiteUrl = "http://gradle.org"
        vcsUrl = "https://github.com/gradle-guides/gradle-site-plugin"
    }
}
