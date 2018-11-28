package org.gradle.plugins.site

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNull

object SitePluginFunctionalTest : Spek({
    val generateSiteTaskName = "generateSiteHtml"
    val defaultOutputPath = "build/docs/site"

    describe("SitePlugin") {
        fun execute(projectDir: File, vararg arguments: String): BuildResult {
            return GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments(arguments.toList())
                    .withPluginClasspath()
                    .build()
        }

        fun assertSiteFiles(directory: File) {
            assert(File(directory, "index.html").isFile)
            assert(File(directory, "css/bootstrap.css").isFile)
            assert(File(directory, "css/bootstrap-responsive.css").isFile)
            assert(File(directory, "img/elephant-corner.png").isFile)
        }

        fun parseIndexHtml(outputDir: File) = Jsoup.parse(File(outputDir, "index.html"), "UTF-8")

        fun websiteLinkAnchor(document: Document) = document.getElementById("website-link")
        fun vcsLinkAnchor(document: Document) = document.getElementById("code-link")
        fun findJavaSourceCompatibility(document: Document) = document.getElementById("java-source-compatibility")
        fun findJavaTargetCompatibility(document: Document) = document.getElementById("java-target-compatibility")

        context("with plugin applied only") {
            val testProjectDir: Path = Files.createTempDirectory("site_plugin_test")
            val buildFile = Files.createFile(testProjectDir.resolve("build.gradle")).toFile()

            buildFile.writeText("""
                plugins {
                    id 'gradle.site'
                }
            """.trimIndent())

            it("provides site task") {
                val buildResult = execute(testProjectDir.toFile(), "tasks", "--all")
                assert(buildResult.output.contains("$generateSiteTaskName - Generates a web page containing information about the project."))
            }

            it("can generate site for default conventions") {
                execute(testProjectDir.toFile(), generateSiteTaskName, "--stacktrace")
                val generatedSiteDir = testProjectDir.resolve(defaultOutputPath).toFile()
                assertSiteFiles(generatedSiteDir)
                val document = parseIndexHtml(generatedSiteDir)

                assertNull(websiteLinkAnchor(document))
                assertNull(vcsLinkAnchor(document))
                assertNull(findJavaSourceCompatibility(document))
                assertNull(findJavaTargetCompatibility(document))
            }

            it("allows output directory specified on CLI") {
                val customOutputDir = "build/clicustom"
                execute(testProjectDir.toFile(), generateSiteTaskName, "-s", "--output-dir=$customOutputDir")
                assertSiteFiles(testProjectDir.resolve(customOutputDir).toFile())
            }
        }

        context("with configured site plugin extension") {
            val testProjectDir = Files.createTempDirectory("site_plugin_test")
            val buildFile = Files.createFile(testProjectDir.resolve("build.gradle")).toFile()

            val customOutputLocation = "build/custom/site"
            val websiteUrl = "https://custom.com"
            val vcsUrl = "https://github.com/gradle-guides/gradle-site-plugin"

            buildFile.writeText("""
                plugins {
                    id 'gradle.site'
                }

                description = 'My description'

                site {
                    outputDir = file('$customOutputLocation')
                    websiteUrl = '$websiteUrl'
                    vcsUrl = '$vcsUrl'
                }

                task assertCustomExtensionProperties {
                    doLast {
                        assert project.extensions.site.outputDir.get().asFile == file('$customOutputLocation')
                        assert project.extensions.site.websiteUrl.get() == '$websiteUrl'
                        assert project.extensions.site.vcsUrl.get() == '$vcsUrl'
                    }
                }
            """.trimIndent())

            it("can generate site for custom conventions") {
                execute(testProjectDir.toFile(), generateSiteTaskName, "--stacktrace")

                val generatedSiteDir = testProjectDir.resolve(customOutputLocation).toFile()
                assertSiteFiles(generatedSiteDir)

                val document = parseIndexHtml(generatedSiteDir)
                assertEquals(websiteUrl, websiteLinkAnchor(document).attr("href"))
                assertEquals(vcsUrl, vcsLinkAnchor(document).attr("href"))
            }

            it("can query extension properties") {
                // Executing this task should not fail
                execute(testProjectDir.toFile(), "assertCustomExtensionProperties", "--stacktrace")
            }
        }

        context("with Java compatibility set") {
            val testProjectDir = Files.createTempDirectory("site_plugin_test")
            val buildFile = Files.createFile(testProjectDir.resolve("build.gradle")).toFile()

            val javaSourceCompatibility = "1.9"
            val javaTargetCompatibility = "1.8"

            buildFile.writeText("""
                    plugins {
                        id 'gradle.site'
                        id 'java'
                    }
                    sourceCompatibility = '$javaSourceCompatibility'
                    targetCompatibility = '$javaTargetCompatibility'
                """.trimIndent())

            it("can derive and render Java-specific information for java plugin") {
                execute(testProjectDir.toFile(), generateSiteTaskName, "--stacktrace")

                val generatedSiteDir = testProjectDir.resolve(defaultOutputPath).toFile()
                assertSiteFiles(generatedSiteDir)

                val document = parseIndexHtml(generatedSiteDir)
                assertEquals(javaSourceCompatibility, findJavaSourceCompatibility(document).text())
                assertEquals(javaTargetCompatibility, findJavaTargetCompatibility(document).text())
            }
        }
    }
})
