/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.docs.guides

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.docs.DocumentationExtension
import org.gradle.docs.guides.internal.GuidesDocumentationPlugin

import javax.inject.Inject

import static org.gradle.docs.internal.StringUtils.toLowerCamelCase

/**
 * The guides base plugin provides conventions for all Gradle guides.
 * <p>
 * Adds the custom attributes to {@link org.asciidoctor.gradle.AsciidoctorTask} for reference in Asciidoc files:
 * <ul>
 *     <li>{@literal samplescodedir}: The directory containing samples code defined as {@literal "$projectDir/samples/code"}</li>
 *     <li>{@literal samplescodedir}: The directory containing expected samples output defined as {@literal "$projectDir/samples/output"}</li>
 * </ul>
 */
@CompileStatic
class BasePlugin implements Plugin<Project> {

    static final String GUIDE_EXTENSION_NAME = 'guide'

    void apply(Project project) {
        project.getPluginManager().apply(GuidesDocumentationPlugin.class);

        def guides = addGuidesExtension(project)
        addTestJvmCode(project)
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException()
    }

    private Guide addGuidesExtension(Project project) {
        Guide result = project.getExtensions().getByType(DocumentationExtension.class).guides.publishedGuides.create(toLowerCamelCase(project.name))
        project.getExtensions().add(Guide, GUIDE_EXTENSION_NAME, result);
        result.description.set(result.title)
        result.guideDirectory.set(project.projectDir)
        result.permalink.set(project.name)
        return result
    }

    void addTestJvmCode(Project project) {
        project.pluginManager.apply(TestJvmCodePlugin)
    }
}
