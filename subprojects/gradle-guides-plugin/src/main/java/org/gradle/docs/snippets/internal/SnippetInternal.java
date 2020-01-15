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

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.internal.components.AssembleDocumentationComponent;
import org.gradle.docs.internal.components.NamedDocumentationComponent;
import org.gradle.docs.snippets.Snippet;

import javax.inject.Inject;

import static org.gradle.docs.internal.StringUtils.capitalize;

public abstract class SnippetInternal extends NamedDocumentationComponent implements AssembleDocumentationComponent, Snippet {
    private final TaskProvider<Task> assemble;

    @Inject
    public SnippetInternal(String name, TaskContainer tasks) {
        super(name);
        this.assemble = tasks.register("assemble" + capitalize(name + "Snippet"));
    }

    @Override
    public TaskProvider<Task> getAssembleTask() {
        return assemble;
    }

    public abstract DirectoryProperty getSnippetDirectory();

    public abstract ConfigurableFileCollection getGroovyContent();

    public abstract ConfigurableFileCollection getKotlinContent();

    public abstract DirectoryProperty getInstallDirectory();
}
