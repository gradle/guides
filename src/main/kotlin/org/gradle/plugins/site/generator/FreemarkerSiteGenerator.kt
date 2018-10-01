package org.gradle.plugins.site.generator

import freemarker.template.Configuration
import freemarker.template.TemplateException
import freemarker.template.TemplateExceptionHandler
import org.gradle.api.GradleException
import org.gradle.plugins.site.data.CustomData
import org.gradle.plugins.site.data.ProjectDescriptor
import org.gradle.plugins.site.utils.FileUtils
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * A site generator implementation based on [Freemarker](http://freemarker.org/).
 */
class FreemarkerSiteGenerator(private val outputDir: File) : SiteGenerator {

    override fun generate(projectDescriptor: ProjectDescriptor, customData: CustomData) {
        try {
            copyCssResources()
            copyImgResources()
            processIndexPageTemplate(projectDescriptor, customData)
        } catch (e: Exception) {
            throw GradleException("Unable to generate site", e)
        }

    }

    @Throws(IOException::class)
    private fun copyCssResources() {
        val resources = ArrayList<String>()
        resources.add("bootstrap.css")
        resources.add("bootstrap-responsive.css")
        copyResources("css", resources)
    }

    @Throws(IOException::class)
    private fun copyImgResources() {
        val resources = ArrayList<String>()
        resources.add("elephant-corner.png")
        copyResources("img", resources)
    }

    @Throws(IOException::class)
    private fun copyResources(subdir: String, resources: List<String>) {
        val targetDir = File(outputDir, subdir)
        FileUtils.createDirectory(targetDir)

        for (resource in resources) {
            val sourcePath = "$subdir/$resource"
            FileUtils.copyFile(resolveAsUrl(sourcePath), File(targetDir, resource))
        }
    }

    private fun resolveAsUrl(name: String): InputStream {
        return javaClass.classLoader.getResourceAsStream(name)
    }

    @Throws(IOException::class, TemplateException::class)
    private fun processIndexPageTemplate(projectDescriptor: ProjectDescriptor, customData: CustomData) {
        val cfg = Configuration(Configuration.VERSION_2_3_25)
        cfg.setClassLoaderForTemplateLoading(javaClass.classLoader, "template")
        cfg.defaultEncoding = "UTF-8"
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        cfg.logTemplateExceptions = false
        val root = HashMap<String, Any>()
        root["project"] = projectDescriptor
        root["customData"] = customData
        val template = cfg.getTemplate("index.ftl")
        template.process(root, FileWriter(File(outputDir, "index.html")))
    }
}
