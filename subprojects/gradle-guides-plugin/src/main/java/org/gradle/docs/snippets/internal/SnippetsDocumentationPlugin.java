/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.docs.snippets.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.docs.internal.components.DocumentationArchiveComponent;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;
import org.gradle.docs.Dsl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.gradle.docs.internal.configure.Asserts.assertNameDoesNotContainsDisallowedCharacters;
import static org.gradle.docs.internal.StringUtils.*;
import static org.gradle.docs.internal.StringUtils.toKebabCase;

public class SnippetsDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();
        ObjectFactory objects = project.getObjects();
        TaskContainer tasks = project.getTasks();

        project.getPluginManager().apply(DocumentationBasePlugin.class);

        // Register a snippets extension to configure published snippets
        SnippetsInternal extension = configureSnippetsExtension(project, layout);

        // Samples
        // Generate wrapper files that can be shared by all samples
        extension.getPublishedSnippets().configureEach(snippet -> applyConventionsForSnippets(extension, snippet));

        // Trigger everything by realizing sample container
        FileTree wrapperFiles = createWrapperFiles(tasks, objects);
        project.afterEvaluate(p -> realizeSnippets(extension, objects, layout, wrapperFiles));
    }

    private FileTree createWrapperFiles(TaskContainer tasks, ObjectFactory objectFactory) {
        TaskProvider<Wrapper> wrapper = tasks.register("generateWrapperForSnippets", Wrapper.class, task -> {
            task.setDescription("Generates wrapper for snippets.");
            // TODO: This ignores changes to the temporary directory
            task.setJarFile(new File(task.getTemporaryDir(), "gradle/wrapper/gradle-wrapper.jar"));
            task.setScriptFile(new File(task.getTemporaryDir(), "gradlew"));
        });
        ConfigurableFileCollection wrapperFiles = objectFactory.fileCollection();
        wrapperFiles.from(wrapper.map(AbstractTask::getTemporaryDir));
        wrapperFiles.builtBy(wrapper);
        return wrapperFiles.getAsFileTree();
    }

    private SnippetsInternal configureSnippetsExtension(Project project, ProjectLayout layout) {
        SnippetsInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSnippets();

        extension.getSnippetsRoot().convention(layout.getProjectDirectory().dir("src/docs/snippets"));

        // TODO: Only in sample and snippets
        extension.getInstallRoot().convention(layout.getBuildDirectory().dir("working/snippets/install"));

        return extension;
    }

    private void applyConventionsForSnippets(SnippetsInternal extension, SnippetInternal snippet) {
        String name = snippet.getName();
        snippet.getSnippetDirectory().convention(extension.getSnippetsRoot().dir(toKebabCase(name)));

        snippet.getInstallDirectory().convention(extension.getInstallRoot().dir(toKebabCase(name)));

        snippet.getDsls().convention(snippet.getSnippetDirectory().map(sampleDirectory -> {
            List<Dsl> dsls = new ArrayList<>();
            for (Dsl dsl : Dsl.values()) {
                if (sampleDirectory.dir(dsl.getConventionalDirectory()).getAsFile().exists()) {
                    dsls.add(dsl);
                }
            }
            return dsls;
        }));

        snippet.getGroovyContent().from(snippet.getSnippetDirectory().dir(Dsl.GROOVY.getConventionalDirectory()));
        snippet.getKotlinContent().from(snippet.getSnippetDirectory().dir(Dsl.KOTLIN.getConventionalDirectory()));
//
//        // TODO: The guides should have the same thing
//        sample.getAssembleTask().configure(task -> {
//            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
//            task.setDescription("Assembles '" + sample.getName() + "' sample.");
//        });
//        sample.getCheckTask().configure(task -> {
//            task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
//            task.setDescription("Checks '" + sample.getName() + "' sample.");
//        });
    }

    private DocumentationArchiveComponent newSnippetBinaryForDsl(SnippetsInternal extension, SnippetInternal snippet, Dsl dsl, ObjectFactory objects, ProjectLayout layout, FileTree wrapperFiles) {
        DocumentationArchiveComponent binary = objects.newInstance(DocumentationArchiveComponent.class, "snippet" + capitalize(snippet.getName()) + dsl.getDisplayName());
        binary.getDsl().convention(dsl).disallowChanges();
        binary.getBaseName().convention("snippet_" + toSnakeCase(snippet.getName()));
        binary.getWorkingDirectory().convention(snippet.getInstallDirectory().dir(dsl.getConventionalDirectory())).disallowChanges();
        binary.getZipInstallDirectory().convention(layout.getBuildDirectory().dir("working/snippets/zips"));
        switch (dsl) {
            case GROOVY:
                binary.getDslSpecificContent().from(snippet.getGroovyContent()).disallowChanges();
                break;
            case KOTLIN:
                binary.getDslSpecificContent().from(snippet.getKotlinContent()).disallowChanges();
                break;
            default:
                throw new GradleException("Unhandled Dsl type " + dsl + " for sample '" + snippet.getName() + "'");
        }
        binary.getContent().from(binary.getDslSpecificContent());
        binary.getContent().from(wrapperFiles);
        binary.getContent().disallowChanges();

        binary.getExcludes().convention(Arrays.asList("**/build/**", "**/.gradle/**"));

        return binary;
    }

    private void realizeSnippets(SnippetsInternal extension, ObjectFactory objects, ProjectLayout layout, FileTree wrapperFiles) {
        // TODO: Project is passed strictly for zipTree method
        // TODO: Disallow changes to published samples container after this point.
        for (SnippetInternal snippet : extension.getPublishedSnippets()) {
            assertNameDoesNotContainsDisallowedCharacters(snippet, "Snippet '%s' has disallowed characters", snippet.getName());

            snippet.getDsls().disallowChanges();
            // TODO: This should only be enforced if we are trying to build the given sample
            Set<Dsl> dsls = snippet.getDsls().get();
            if (dsls.isEmpty()) {
                throw new GradleException("Samples must have at least one DSL, sample '" + snippet.getName() + "' has none.");
            }
            for (Dsl dsl : dsls) {
                DocumentationArchiveComponent binary = newSnippetBinaryForDsl(extension, snippet, dsl, objects, layout, wrapperFiles);
                extension.getBinaries().add(binary);
                snippet.getAssembleTask().configure(task -> task.dependsOn(binary.getZipFile()));
//                snippet.getCheckTask().configure(task -> task.dependsOn(binary.getValidationReport()));
            }

            extension.getBinaries().add(snippet);
        }
    }
}
