package org.gradle.plugins.site.generator

import freemarker.template.Configuration
import freemarker.template.TemplateException
import freemarker.template.TemplateExceptionHandler
import org.gradle.api.GradleException
import org.gradle.plugins.site.data.ProjectDescriptor
import org.gradle.plugins.site.data.ProjectLinksDescriptor
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * A site generator implementation based on [Freemarker](https://freemarker.org/).
 */
class FreemarkerSiteGenerator(private val outputDir: File) : SiteGenerator {

    override fun generate(projectDescriptor: ProjectDescriptor, projectLinksDescriptor: ProjectLinksDescriptor) {
        try {
            copyCssResources()
            copyImgResources()
            processIndexPageTemplate(projectDescriptor, projectLinksDescriptor)
        } catch (e: Exception) {
            throw GradleException("Unable to generate site", e)
        }

    }

    @Throws(IOException::class)
    private fun copyCssResources() {
        val resources = listOf(
                "bootstrap.css",
                "bootstrap-responsive.css"
        )
        copyResources("css", resources)
    }

    @Throws(IOException::class)
    private fun copyImgResources() {
        val resources = listOf(
                "elephant-corner.png"
        )
        copyResources("img", resources)
    }

    @Throws(IOException::class)
    private fun copyResources(subdir: String, resources: List<String>) {
        val targetDir = outputDir.resolve(subdir)
        for (resource in resources) {
            resolveAsStream("$subdir/$resource").use { inputStream ->
                inputStream.copyToFile(targetDir.resolve(resource))
            }
        }
    }

    private fun resolveAsStream(name: String): InputStream {
        return javaClass.classLoader.getResourceAsStream(name).buffered()
    }

    @Throws(IOException::class)
    private fun InputStream.copyToFile(file: File) {
        file.parentFile.let { dir ->
            if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Unable to create directory $dir")
        }
        file.outputStream().buffered().use { outputStream ->
            copyTo(outputStream)
        }
    }

    @Throws(IOException::class, TemplateException::class)
    private fun processIndexPageTemplate(projectDescriptor: ProjectDescriptor, linksDescriptor: ProjectLinksDescriptor) {
        val cfg = Configuration(Configuration.VERSION_2_3_25).apply {
            setClassLoaderForTemplateLoading(javaClass.classLoader, "template")
            defaultEncoding = "UTF-8"
            templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
            logTemplateExceptions = false
        }
        val root = mapOf(
                "project" to projectDescriptor,
                "customData" to linksDescriptor
        )
        val template = cfg.getTemplate("index.ftl")
        outputDir.resolve("index.html").bufferedWriter().use { writer ->
            template.process(root, writer)
        }
    }
}
