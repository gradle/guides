package org.gradle.docs.internal.configure;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.internal.TestableAsciidoctorContentBinary;
import org.gradle.docs.internal.TestableRenderedContentLinksBinary;
import org.gradle.docs.internal.ViewableContentBinary;
import org.gradle.docs.internal.exemplar.AsciidoctorContentTest;
import org.gradle.docs.internal.exemplar.AsciidoctorContentTestConsoleType;
import org.gradle.docs.internal.tasks.CheckLinks;
import org.gradle.docs.internal.tasks.ViewDocumentation;
import org.gradle.docs.samples.internal.SampleContentBinary;
import org.gradle.docs.samples.internal.tasks.GenerateSamplePageAsciidoc;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.gradle.docs.internal.DocumentationBasePlugin.DOCUMENTATION_GROUP_NAME;
import static org.gradle.docs.internal.StringUtils.capitalize;

public class ContentBinaries {
    public static void createTasksForSampleContentBinary(TaskContainer tasks, SampleContentBinary binary) {
        TaskProvider<GenerateSamplePageAsciidoc> generateSamplePage = tasks.register("generate" + capitalize(binary.getName()) + "Page", GenerateSamplePageAsciidoc.class, task -> {
            task.setDescription("Generates asciidoc page for sample '" + binary.getName() + "'");

            task.getAttributes().put("samples-dir", binary.getSampleInstallDirectory().get().getAsFile().getAbsolutePath());
            task.getAttributes().put("gradle-version", binary.getGradleVersion());

            task.getSampleSummary().convention(binary.getSummary());
            task.getReadmeFile().convention(binary.getSourcePageFile());
            task.getOutputFile().fileProvider(binary.getBaseName().map(fileName -> new File(task.getProject().getBuildDir(), "/tmp/" + fileName + ".adoc")));
        });
        binary.getIndexPageFile().convention(generateSamplePage.flatMap(GenerateSamplePageAsciidoc::getOutputFile));
    }

    public static void createTasksForContentBinary(TaskContainer tasks, ViewableContentBinary binary) {
        tasks.register(binary.getViewTaskName(), ViewDocumentation.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP_NAME);
            task.setDescription("Open in the browser the rendered documentation");
            task.getIndexFile().convention(binary.getViewablePageFile());
        });
    }

    public static void createCheckTasksForContentBinary(TaskContainer tasks, TestableRenderedContentLinksBinary binary, TaskProvider<Task> check) {
        TaskProvider<CheckLinks> checkLinksTask = tasks.register(binary.getCheckLinksTaskName(), CheckLinks.class, task -> {
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            task.setDescription("Check for any dead link in the rendered documentation");
            task.getIndexDocument().convention(binary.getRenderedPageFile());
            task.getTimeout().convention(Duration.ofMinutes(30));
        });

        check.configure(it -> it.dependsOn(checkLinksTask));
    }

    public static void createCheckTaskForAsciidoctorContentBinary(Project project, String taskName, Collection<? extends TestableAsciidoctorContentBinary> binaries, TaskProvider<Task> check, Configuration asciidoctorClasspath) {
        Configuration configuration = project.getConfigurations().create(taskName);
        configuration.extendsFrom(asciidoctorClasspath);
        DependencyHandler dependencies = project.getDependencies();
        dependencies.add(configuration.getName(), "org.gradle:gradle-tooling-api:6.0.1");
        dependencies.add(configuration.getName(), "org.apache.commons:commons-lang3:3.9");
        dependencies.add(configuration.getName(), "org.gradle:sample-check:0.12.6");
        dependencies.add(configuration.getName(), "junit:junit:4.12");

        TaskProvider<AsciidoctorContentTest> asciidoctorContentDocsTest = project.getTasks().register(taskName, AsciidoctorContentTest.class, task -> {
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            task.setDescription("Check Asciidoctor content steps commands.");
            task.getClasspath().from(configuration);
            task.getGradleVersion().convention(project.getGradle().getGradleVersion());
            task.getDefaultConsoleType().convention(taskName.contains("Sample") ? AsciidoctorContentTestConsoleType.RICH : AsciidoctorContentTestConsoleType.PLAIN);
            binaries.forEach(binary -> {
                task.testCase(testCase -> {
                    testCase.getContentFile().set(binary.getContentFile());
                    testCase.getStartingSample().set(binary.getStartingSampleDirectory());
                });
            });
        });

        check.configure(it -> it.dependsOn(asciidoctorContentDocsTest));
    }
}
