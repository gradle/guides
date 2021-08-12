import java.net.URL
import org.apache.tools.ant.taskdefs.condition.Os
import org.apache.commons.io.FileUtils

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("commons-io:commons-io:2.5")
    }
}

plugins {
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.gebish:geb-core:1.1.1")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.4.0")
    testImplementation("org.seleniumhq.selenium:selenium-support:2.52.0")
    testImplementation("io.ratpack:ratpack-groovy-test:1.5.4")
    testImplementation("org.gebish:geb-junit4:1.1.1")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.25")
}

tasks {
    val chromeDriverVersion = "2.33"

    val downloadChromeDriver by registering {
        val outputFile = file("$buildDir/webdriver/chromedriver.zip")
        inputs.property("chromeDriverVersion", chromeDriverVersion)
        outputs.file(outputFile)

        doLast {
            val driverOsFilenamePart = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                "win32"
            } else if (Os.isFamily(Os.FAMILY_MAC)) {
                "mac64"
            } else if (Os.isFamily(Os.FAMILY_UNIX)) {
                if (Os.isArch("amd64")) {
                    "linux64"
                } else {
                    "linux32"
                }
            } else {
                throw GradleException("Unknown operating system")
            }
            FileUtils.copyURLToFile(
                URL("https://chromedriver.storage.googleapis.com/${chromeDriverVersion}/chromedriver_${driverOsFilenamePart}.zip"),
                outputFile
            )
        }
    }

    val unzipChromeDriver by registering(Copy::class) {
        val outputDir = file("$buildDir/webdriver/chromedriver")
        dependsOn(downloadChromeDriver)
        outputs.dir(outputDir)

        from(provider { zipTree(downloadChromeDriver.get().outputs.files.singleFile) })
        into(outputDir)
    }

    test {
        enabled = false
    }

    register<Test>("takeScreenshots") {
        val screenshotDir = file("${buildDir}/screenshots")
        outputs.dir(screenshotDir).withPropertyName("screenshotDir")
        outputs.upToDateWhen { false }
        outputs.doNotCacheIf("Depending on external service") { true }

        jvmArgumentProviders += CommandLineArgumentProvider { listOf("-Dscreenshot.dir=$screenshotDir") }
        jvmArgumentProviders += WebdriverProvider(unzipChromeDriver.map { it.outputs.files.singleFile })

        listOf("dev", "dogfood", "cache-admin").forEach { name ->
            val prefix = "scans.${name}"
            systemProperty("${prefix}.host", findProperty("${prefix}.host").let { "$it" })
            systemProperty("${prefix}.username", findProperty("${prefix}.username").let { "$it" })
            systemProperty("${prefix}.password", findProperty("${prefix}.password").let { "$it" })
        }
    }
}

class WebdriverProvider(@Input @PathSensitive(PathSensitivity.NONE) val executableDir: Provider<File>) :
    CommandLineArgumentProvider {
    override fun asArguments(): List<String> {
        val chromedriverFilename = if (Os.isFamily(Os.FAMILY_WINDOWS)) "chromedriver.exe" else "chromedriver"
        val chromedriverExecutable = File(executableDir.get(), chromedriverFilename)
        return listOf("-Dwebdriver.chrome.driver=${chromedriverExecutable.absolutePath}")
    }
}
