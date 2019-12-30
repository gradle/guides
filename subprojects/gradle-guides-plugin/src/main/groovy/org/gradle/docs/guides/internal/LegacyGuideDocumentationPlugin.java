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

package org.gradle.docs.guides.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.docs.DocumentationExtension;
import org.gradle.docs.guides.Guide;

import javax.inject.Inject;

import static org.gradle.docs.internal.StringUtils.toLowerCamelCase;

/**
 * The guides base plugin provides conventions for all Gradle guides.
 * <p>
 * Adds the custom attributes to {@link org.asciidoctor.gradle.AsciidoctorTask} for reference in Asciidoc files:
 * <ul>
 *     <li>{@literal samplescodedir}: The directory containing samples code defined as {@literal "$projectDir/samples/code"}</li>
 *     <li>{@literal samplesoutputdir}: The directory containing expected samples output defined as {@literal "$projectDir/samples/output"}</li>
 * </ul>
 */
public class LegacyGuideDocumentationPlugin implements Plugin<Project> {

    static final String GUIDE_EXTENSION_NAME = "guide";

    public void apply(Project project) {
        project.getPluginManager().apply(GuidesDocumentationPlugin.class);

        addGuidesExtension(project);
        addTestJvmCode(project);
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException();
    }

    private Guide addGuidesExtension(Project project) {
        Guide result = project.getExtensions().getByType(DocumentationExtension.class).getGuides().getPublishedGuides().create(toLowerCamelCase(project.getName()));
        project.getExtensions().add(Guide.class, GUIDE_EXTENSION_NAME, result);
        result.getDescription().set(result.getTitle());
        result.getGuideDirectory().set(project.getProjectDir());
        result.getPermalink().set(project.getName());
        return result;
    }

    private void addTestJvmCode(Project project) {
        project.getPluginManager().apply(TestJvmCodePlugin.class);
    }
}
