package org.gradle.plugins.site.data;

import org.gradle.api.GradleException;
import org.gradle.plugins.site.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SiteGenerator {

    private final File outputDir;

    public SiteGenerator(File outputDir) {
        this.outputDir = outputDir;
    }

    public void generate(ProjectDescriptor projectDescriptor) {
        try {
            copyCssResources();
            copyJsResources();
            FileUtils.writeFile(new File(outputDir, "index.html"), projectDescriptor.getName());
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
}
