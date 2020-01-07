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
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.testing.Test;
import org.gradle.docs.DocumentationExtension;
import org.gradle.docs.guides.Guide;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.gradle.docs.internal.DocumentationBasePlugin.DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME;
import static org.gradle.docs.internal.DocumentationBasePlugin.DOCS_TEST_TASK_NAME;
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
    public static final String SAMPLES_BASE_DIR = "samples";
    public static final String GUIDE_EXTENSION_NAME = "guide";

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
        result.getDescription().set(result.getDisplayName());
        result.getGuideDirectory().set(project.getProjectDir());
        result.getPermalink().set(project.getName());
        return result;
    }

    /**
     * Configure JVM code snippets testing.
     */
    private void addTestJvmCode(Project project) {
        project.getPluginManager().apply("groovy");
        project.getPluginManager().apply("base");

        addRepositories(project);
        addDependencies(project);
        configureSamplesConventions(project);
    }

    private void addRepositories(Project project) {
        RepositoryHandler repositoryHandler = project.getRepositories();
        repositoryHandler.maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl("https://repo.gradle.org/gradle/libs-releases"));
    }

    /**
     * Automatically adds the following dependencies to the project applying the plugin:
     * <ul>
     *     <li>Local Groovy bundled with Gradle</li>
     *     <li>Gradle TestKit</li>
     *     <li>Spock core 1.0</li>
     *     <li>Apache Commons IO 2.5</li>
     *     <li>Guides test fixtures</li>
     * </ul>
     */
    private void addDependencies(Project project) {
        DependencyHandler dependencies = project.getDependencies();
        dependencies.add(DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME, dependencies.localGroovy());
        dependencies.add(DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME, dependencies.gradleTestKit());
        ModuleDependency spockDependency = (ModuleDependency)dependencies.add(DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.spockframework:spock-core:1.2-groovy-2.5");
        spockDependency.exclude(mapOf("module", "groovy-all"));
        dependencies.add(DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME, "commons-io:commons-io:2.5");
        ModuleDependency testFixturesDependency = (ModuleDependency)dependencies.add(DOCS_TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.gradle.guides:test-fixtures:0.4");
        testFixturesDependency.exclude(mapOf("module", "spock-core"));
    }

    private static Map<String, String> mapOf(String key, String value) {
        Map<String, String> result = new HashMap<>();
        result.put(key, value);
        return result;
    }

    private void configureSamplesConventions(Project project) {
        // Passes the system property {@literal "samplesDir"} with the value {@literal "$projectDir/samples"} to {@code "test"} task.
        project.getTasks().named(DOCS_TEST_TASK_NAME, Test.class, task -> {
            File samplesBaseDir = project.file(SAMPLES_BASE_DIR);
            task.getInputs().files(samplesBaseDir).withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();
            // This breaks relocatability of the test task. If caching becomes important we should consider redefining the inputs for the test task
            task.systemProperty("samplesDir", samplesBaseDir.getAbsolutePath());
        });
    }
}
