package org.gradle.plugins.site.data;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.gradle.api.GradleException;
import org.gradle.plugins.site.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreemarkerSiteGenerator implements SiteGenerator {

    private final File outputDir;

    public FreemarkerSiteGenerator(File outputDir) {
        this.outputDir = outputDir;
    }

    public void generate(ProjectDescriptor projectDescriptor, CustomData customData) {
        try {
            copyCssResources();
            copyJsResources();
            copyImgResources();
            processIndexPageTemplate(projectDescriptor, customData);
        } catch (Exception e) {
            throw new GradleException("Unable to generate site", e);
        }
    }

    private void copyCssResources() throws IOException, URISyntaxException {
        List<String> resources = new ArrayList<String>();
        resources.add("bootstrap.css");
        resources.add("bootstrap-responsive.css");
        copyResources("css", resources);
    }

    private void copyJsResources() throws IOException, URISyntaxException {
        List<String> resources = new ArrayList<String>();
        resources.add("bootstrap.js");
        copyResources("js", resources);
    }

    private void copyImgResources() throws IOException, URISyntaxException {
        List<String> resources = new ArrayList<String>();
        resources.add("elephant-corner.png");
        copyResources("img", resources);
    }

    private void copyResources(String subdir, List<String> resources) throws IOException, URISyntaxException {
        File targetDir = new File(outputDir, subdir);
        FileUtils.createDirectory(targetDir);

        for (String resource : resources) {
            String sourcePath = subdir + "/" + resource;
            FileUtils.copyFile(new File(resolveAsUrl(sourcePath).toURI()), new File(targetDir, resource));
        }
    }

    private URL resolveAsUrl(String name) {
        return getClass().getClassLoader().getResource(name);
    }

    private void processIndexPageTemplate(ProjectDescriptor projectDescriptor, CustomData customData) throws IOException, URISyntaxException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDirectoryForTemplateLoading(new File(resolveAsUrl("template").toURI()));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("project", projectDescriptor);
        root.put("customData", customData);
        Template template = cfg.getTemplate("index.ftl");
        template.process(root, new FileWriter(new File(outputDir, "index.html")));
    }
}
