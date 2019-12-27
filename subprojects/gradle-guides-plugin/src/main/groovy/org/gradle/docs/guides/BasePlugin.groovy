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
import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.asciidoctor.gradle.AsciidoctorBackend
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
    static final String CHECK_LINKS_TASK = 'checkLinks'

    void apply(Project project) {
        project.getPluginManager().apply(GuidesDocumentationPlugin.class);

        def guides = addGuidesExtension(project)
        addGradleRunnerSteps(project)
        addAsciidoctor(project, guides)
        addGitHubPages(project)
        addCheckLinks(project)
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
        return result
    }

    private void addGradleRunnerSteps(Project project) {
        project.apply plugin : 'org.ysb33r.gradlerunner'
    }

    private void addCheckLinks(Project project) {
        CheckLinks task = project.tasks.create(CHECK_LINKS_TASK, CheckLinks)

        AsciidoctorTask asciidoc = (AsciidoctorTask)(project.tasks.getByName('asciidoctor'))

        task.indexDocument = { project.file("${asciidoc.outputDir}/html5/index.html") }
        task.dependsOn asciidoc

        project.tasks.getByName("check").dependsOn(task)
    }

    @CompileDynamic
    private void addAsciidoctor(Project project, Guide guides) {

        Provider<String> minimumGradleVersion = guides.getMinimumGradleVersion();

        project.getPluginManager().apply("org.asciidoctor.convert");
        project.repositories.maven { url "https://repo.gradle.org/gradle/libs-releases" }
        project.dependencies.add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0")

        TaskProvider<AsciidoctorTask> asciidoc = project.tasks.named("asciidoctor", AsciidoctorTask.class);
        project.afterEvaluate { project.tasks.named("generate${toLowerCamelCase(project.name).capitalize()}Page") { it.onlyIf { project.file('contents').exists() } } }
        project.tasks.getByName('build').dependsOn asciidoc

        Task asciidocAttributes = project.tasks.create('asciidoctorAttributes')
        asciidocAttributes.description = 'Display all Asciidoc attributes that are passed from Gradle'
        asciidocAttributes.group = 'Documentation'
        asciidocAttributes.doLast {
            println 'Current Asciidoctor Attributes'
            println '=============================='
            asciidoc.get().attributes.each { Object k, Object v ->
                println "${k.toString()}: ${v.toString()}"
            }
            println "gradle-version: ${minimumGradleVersion.get()}"
            println "user-manual: https://docs.gradle.org/${minimumGradleVersion.get()}/userguide/"
            println "language-reference: https://docs.gradle.org/${minimumGradleVersion.get()}/dsl/"
            println "api-reference: https://docs.gradle.org/${minimumGradleVersion.get()}/javadoc/"
        }

        asciidoc.configure {task ->
            task.getInputs().property("minimumGradleVersion", minimumGradleVersion);
            task.doFirst {
                task.attributes.put("gradle-version", minimumGradleVersion.get());
                task.attributes.put("user-manual", "https://docs.gradle.org/${minimumGradleVersion.get()}/userguide/");
                task.attributes.put("language-reference", "https://docs.gradle.org/${minimumGradleVersion.get()}/dsl/");
                task.attributes.put("api-reference", "https://docs.gradle.org/${minimumGradleVersion.get()}/javadoc/");
            }

            task.getInputs().property("repositoryPath", guides.repositoryPath);
            task.doFirst {
                task.attributes.put("repository-path", guides.repositoryPath.get());
            }

            task.getInputs().property("guideTitle", guides.title);
            task.doFirst {
                task.attributes.put("guide-title", guides.title.get());
            }

            task.setSourceDir(project.file("contents"));
            task.setOutputDir(project.buildDir);
            task.backends("html5");
            task.getInputs().files("samples").withPropertyName("samplesDir").withPathSensitivity(PathSensitivity.RELATIVE).optional();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("doctype", "book");
            attributes.put("icons", "font");
            attributes.put("source-highlighter", "prettify");
            attributes.put("toc", "auto");
            attributes.put("toclevels", 1);
            attributes.put("toc-title", "Contents");
            // TODO: This is specific to guides
            attributes.put("imagesdir", "images");
            attributes.put("stylesheet", null);
            attributes.put("linkcss", true);
            attributes.put("docinfodir", ".");
            attributes.put("docinfo1", "");
            attributes.put("nofooter", true);
            attributes.put("sectanchors", true);
            attributes.put("sectlinks", true);
            attributes.put("linkattrs", true);
            attributes.put("encoding", "utf-8");
            attributes.put("idprefix", "");
            attributes.put("guides", "https://guides.gradle.org");
            attributes.put("user-manual-name", "User Manual");
            attributes.put("projdir", project.projectDir);
            attributes.put("codedir", project.file("src/main"));
            attributes.put("testdir", project.file("src/test"));
            attributes.put("samplescodedir", project.file("samples/code"));
            attributes.put("samplesoutputdir", project.file("samples/output"));
            attributes.put("samples-dir", project.file("samples"));
            task.attributes(attributes);

            task.sources { include("index.adoc") }
        }

        project.tasks.getByName("asciidoctor").dependsOn project.tasks.getByPath('gradleRunner')
        project.tasks.getByName("guidesMultiPage").dependsOn project.tasks.getByPath('gradleRunner')
        project.tasks.getByName("assemble").dependsOn asciidoc

        // TODO - rework the above to use lazy configuration
    }

    private void addGitHubPages(Project project) {
        project.apply plugin : 'org.ajoberstar.git-publish'

        GitPublishExtension githubPages = (GitPublishExtension)(project.extensions.getByName('gitPublish'))
        Guide guide = (Guide)(project.extensions.getByName(BasePlugin.GUIDE_EXTENSION_NAME))
        AsciidoctorTask asciidoc = (AsciidoctorTask)(project.tasks.getByName('asciidoctor'))
        String ghToken = System.getenv("GRGIT_USER")

        githubPages.branch.set('gh-pages')
        githubPages.commitMessage.set('Publish to GitHub Pages')
        githubPages.contents.from {"${asciidoc.outputDir}/${AsciidoctorBackend.HTML5.id}"}

        project.afterEvaluate {
            if (ghToken) {
                githubPages.repoUri.set(guide.repositoryPath.map { "https://github.com/${it}.git".toString() })
            } else {
                githubPages.repoUri.set(guide.repositoryPath.map { "git@github.com:${it}.git".toString() })
            }
        }
    }
}
