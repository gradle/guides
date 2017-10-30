package org.gradle.plugins.site.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.site.data.CustomData;
import org.gradle.plugins.site.data.ProjectDescriptor;
import org.gradle.plugins.site.generator.FreemarkerSiteGenerator;
import org.gradle.plugins.site.generator.SiteGenerator;

import java.io.File;

/**
 * Custom task for generating a web page containing information derived from the project.
 */
public class SiteGenerate extends DefaultTask {

    private final Property<ProjectDescriptor> projectDescriptor;
    private final Property<File> outputDir;
    private final CustomData customData;

    public SiteGenerate() {
        this.projectDescriptor = getProject().getObjects().property(ProjectDescriptor.class);
        this.outputDir = getProject().getObjects().property(File.class);
        customData = new CustomData(getProject());
    }

    /**
     * Returns the project descriptor containing the derived project information.
     *
     * @return The project descriptor.
     */
    @Nested
    public ProjectDescriptor getProjectDescriptor() {
        return projectDescriptor.get();
    }

    /**
     * Configures the project descriptor containing the derived project information.
     *
     * @param projectDescriptor The project descriptor.
     */
    public void setProjectDescriptor(ProjectDescriptor projectDescriptor) {
        this.projectDescriptor.set(projectDescriptor);
    }

    /**
     * Configures the {@link org.gradle.api.provider.Provider} calculating the project descriptor containing the derived project information.
     *
     * @param projectDescriptor The provider calculating the project descriptor.
     */
    public void setProjectDescriptor(Provider<ProjectDescriptor> projectDescriptor) {
        this.projectDescriptor.set(projectDescriptor);
    }

    /**
     * Returns the output directory for the generated web page.
     *
     * @return The output directory.
     */
    @OutputDirectory
    public File getOutputDir() {
        return outputDir.get();
    }

    /**
     * Configures the output directory for the generated web page.
     *
     * @param outputDir The output directory.
     */
    public void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir);
    }

    /**
     * Configures the{@link org.gradle.api.provider.Provider} calculating the output directory for the generated web page.
     *
     * @param outputDir The provider calculating the output directory.
     */
    public void setOutputDir(Provider<File> outputDir) {
        this.outputDir.set(outputDir);
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
        SiteGenerator siteGenerator = new FreemarkerSiteGenerator(getOutputDir());
        siteGenerator.generate(getProjectDescriptor(), customData);
    }
}
