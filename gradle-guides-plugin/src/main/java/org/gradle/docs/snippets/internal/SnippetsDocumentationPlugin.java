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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.docs.internal.DocumentationBasePlugin;
import org.gradle.docs.internal.DocumentationExtensionInternal;

import static org.gradle.docs.internal.Asserts.assertNameDoesNotContainsDisallowedCharacters;

public class SnippetsDocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ProjectLayout layout = project.getLayout();

        project.getPluginManager().apply(DocumentationBasePlugin.class);

        // Register a samples extension to configure published samples
        SnippetsInternal extension = configureSnippetsExtension(project, layout);

        // Trigger everything by realizing sample container
        project.afterEvaluate(p -> realizeSnippets(extension));
    }

    private SnippetsInternal configureSnippetsExtension(Project project, ProjectLayout layout) {
        SnippetsInternal extension = project.getExtensions().getByType(DocumentationExtensionInternal.class).getSnippets();

        extension.getSnippetsRoot().convention(layout.getProjectDirectory().dir("src/docs/snippets"));

        return extension;
    }

    private void realizeSnippets(SnippetsInternal extension) {
        // TODO: Project is passed strictly for zipTree method
        // TODO: Disallow changes to published samples container after this point.
        for (SnippetInternal snippet : extension.getPublishedSnippets()) {
            assertNameDoesNotContainsDisallowedCharacters(snippet, "Snippet '%s' has disallowed characters", snippet.getName());
        }
    }
}
