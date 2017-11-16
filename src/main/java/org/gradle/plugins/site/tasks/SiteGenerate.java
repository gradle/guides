package org.gradle.plugins.site.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.site.data.CustomData;
import org.gradle.plugins.site.data.ProjectDescriptor;
import org.gradle.plugins.site.generator.FreemarkerSiteGenerator;
import org.gradle.plugins.site.generator.SiteGenerator;

/**
 * Custom task for generating a web page containing information derived from the project.
 */
public class SiteGenerate extends DefaultTask {

    private final Property<ProjectDescriptor> projectDescriptor;
    private final DirectoryProperty outputDir;
    private final CustomData customData;

    public SiteGenerate() {
        this.projectDescriptor = getProject().getObjects().property(ProjectDescriptor.class);
        this.outputDir = newOutputDirectory();
        customData = new CustomData(getProject());
    }

    /**
     * Returns the project descriptor containing the derived project information.
     *
     * @return The project descriptor.
     */
    @Nested
    public Property<ProjectDescriptor> getProjectDescriptor() {
        return projectDescriptor;
    }

    /**
     * Returns the output directory for the generated web page.
     *
     * @return The output directory.
     */
    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    /**
     * Returns the custom data to be used in the generated web page.
     *
     * @return The custom data.
     */
    @Nested
    public CustomData getCustomData() {
        return customData;
    }

    @TaskAction
    public void generate() {
        SiteGenerator siteGenerator = new FreemarkerSiteGenerator(getOutputDir().get().getAsFile());
        siteGenerator.generate(getProjectDescriptor().get(), customData);
    }
}
