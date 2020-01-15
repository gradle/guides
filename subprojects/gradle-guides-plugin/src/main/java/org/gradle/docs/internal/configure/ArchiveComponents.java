package org.gradle.docs.internal.configure;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.internal.components.DocumentationArchiveComponent;
import org.gradle.docs.internal.tasks.InstallDocumentation;
import org.gradle.docs.internal.tasks.ValidateDocumentationArchive;
import org.gradle.docs.internal.tasks.ZipDocumentation;

import java.util.concurrent.Callable;

import static org.gradle.docs.internal.StringUtils.capitalize;

public class ArchiveComponents {
    public static void createTasksForArchiveBinary(TaskContainer tasks, ProjectLayout layout, DocumentationArchiveComponent binary) {
        TaskProvider<ValidateDocumentationArchive> validateSample = tasks.register("validate" + capitalize(binary.getName()), ValidateDocumentationArchive.class, task -> {
            task.setDescription("Checks the sample '" + binary.getName() + "' is valid.");
            task.getDsl().convention(binary.getDsl());
            task.getDocumentationComponentName().convention(binary.getName());
            task.getRequiredContentPaths().convention(binary.getRequiredContentPaths());
            task.getZipFile().convention(binary.getZipFile());
            task.getReportFile().convention(layout.getBuildDirectory().file("reports/documentation-validation/" + task.getName() + ".txt"));
        });
        binary.getValidationReport().convention(validateSample.flatMap(ValidateDocumentationArchive::getReportFile));

        TaskProvider<InstallDocumentation> installSampleTask = tasks.register("install" + capitalize(binary.getName()), InstallDocumentation.class, task -> {
            task.setDescription("Installs '" + binary.getName() + "' into a local directory.");
            // TODO: zipTree should be lazy
            task.dependsOn(binary.getZipFile());
            task.getReadmeFile().convention(binary.getReadmeFile());
            task.getSource().from((Callable<FileTree>)() -> task.getProject().zipTree(binary.getZipFile()));
            task.getInstallDirectory().convention(binary.getWorkingDirectory());
        });
        binary.getInstallDirectory().convention(installSampleTask.flatMap(InstallDocumentation::getInstallDirectory));

        TaskProvider<ZipDocumentation> zipTask = tasks.register("zip" + capitalize(binary.getName()), ZipDocumentation.class, task -> {
            task.setDescription("Creates a zip for '" + binary.getName() + "'.");
            task.getSource().from(binary.getContent());
            task.getReadmeFile().convention(binary.getReadmeFile());
            task.getMainSource().from(binary.getDslSpecificContent());
            task.getExcludes().convention(binary.getExcludes());
            task.getArchiveFile().convention(binary.getZipInstallDirectory().file(binary.getBaseName().map(name -> String.format("%s-%s.zip", name, binary.getDsl().get().getDslLabel()))));
        });
        binary.getZipFile().convention(zipTask.flatMap(ZipDocumentation::getArchiveFile));
    }
}
