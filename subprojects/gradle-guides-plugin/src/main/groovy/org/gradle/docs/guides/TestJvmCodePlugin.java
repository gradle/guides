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

package org.gradle.docs.guides;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.testing.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.gradle.api.plugins.JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME;

/**
 * Plugin that is used to test JVM code snippets. Sets source and target compatibility to Java 1.7.
 * <p>
 * Automatically adds the following dependencies to the project applying the plugin:
 * <ul>
 *     <li>Local Groovy bundled with Gradle</li>
 *     <li>Gradle TestKit</li>
 *     <li>Spock core 1.0</li>
 *     <li>Apache Commons IO 2.5</li>
 *     <li>Guides test fixtures</li>
 * </ul>
 * <p>
 * Passes the system property {@literal "samplesDir"} with the value {@literal "$projectDir/samples"} to {@code "test"} task.
 *
 * @since 0.1
 */
public class TestJvmCodePlugin implements Plugin<Project> {
    public static final String SAMPLES_BASE_DIR = "samples";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("groovy");
        project.getPluginManager().apply("base");

        setJDKVersion(project);
        addRepositories(project);
        addDependencies(project);
        configureSamplesConventions(project);
    }

    private void setJDKVersion(Project project) {
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        javaConvention.setSourceCompatibility(JavaVersion.VERSION_1_8);
        javaConvention.setTargetCompatibility(JavaVersion.VERSION_1_8);
    }

    private void addRepositories(Project project) {
        RepositoryHandler repositoryHandler = project.getRepositories();
        repositoryHandler.maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl("https://repo.gradle.org/gradle/libs-releases"));
    }

    private void addDependencies(Project project) {
        DependencyHandler dependencies = project.getDependencies();
        dependencies.add(TEST_COMPILE_CONFIGURATION_NAME, dependencies.localGroovy());
        dependencies.add(TEST_COMPILE_CONFIGURATION_NAME, dependencies.gradleTestKit());
        ModuleDependency spockDependency = (ModuleDependency)dependencies.add(TEST_COMPILE_CONFIGURATION_NAME, "org.spockframework:spock-core:1.2-groovy-2.5");
        spockDependency.exclude(mapOf("module", "groovy-all"));
        dependencies.add(TEST_COMPILE_CONFIGURATION_NAME, "commons-io:commons-io:2.5");
        ModuleDependency testFixturesDependency = (ModuleDependency)dependencies.add(TEST_COMPILE_CONFIGURATION_NAME, "org.gradle.guides:test-fixtures:0.4");
        testFixturesDependency.exclude(mapOf("module", "spock-core"));
    }

    private static Map<String, String> mapOf(String key, String value) {
        Map<String, String> result = new HashMap<>();
        result.put(key, value);
        return result;
    }

    private void configureSamplesConventions(Project project) {
        Test testTask = (Test)project.getTasks().getByName(JavaPlugin.TEST_TASK_NAME);
        File samplesBaseDir = project.file(SAMPLES_BASE_DIR);
        testTask.getInputs().files(samplesBaseDir).withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();
        // This breaks relocatability of the test task. If caching becomes important we should consider redefining the inputs for the test task
        testTask.systemProperty("samplesDir", samplesBaseDir.getAbsolutePath());
    }
}
