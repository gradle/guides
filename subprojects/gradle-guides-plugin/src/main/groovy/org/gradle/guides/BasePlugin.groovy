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

package org.gradle.guides

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.asciidoctor.gradle.AsciidoctorBackend
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

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
        project.apply plugin : 'lifecycle-base'

        def guides = addGuidesExtension(project)
        addGradleRunnerSteps(project)
        addAsciidoctor(project, guides)
        addGitHubPages(project)
        addCloudCI(project)
        addCheckLinks(project)
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException()
    }

    private GuidesExtension addGuidesExtension(Project project) {
        GuidesExtension result = project.extensions.create(GUIDE_EXTENSION_NAME, GuidesExtension)
        result.repositoryPath.convention("gradle-guides/${project.name}".toString())
        result.minimumGradleVersion.convention(project.gradle.gradleVersion)
        result.title.convention(result.repositoryPath.map { project.name.split('-').collect { it.capitalize() }.join(' ') })
        result.description.convention(result.title)
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
    }

    @CompileDynamic
    private void addAsciidoctor(Project project, GuidesExtension guides) {

        def minimumGradleVersion = guides.minimumGradleVersion

        project.apply plugin: 'org.asciidoctor.convert'
        project.repositories.maven { url "https://repo.gradle.org/gradle/libs-releases" }
        project.dependencies.add("asciidoctor", "org.gradle:docs-asciidoctor-extensions:0.4.0")

        AsciidoctorTask asciidoc = (AsciidoctorTask) (project.tasks.getByName('asciidoctor'))
        project.tasks.getByName('build').dependsOn asciidoc

        Task asciidocAttributes = project.tasks.create('asciidoctorAttributes')
        asciidocAttributes.description = 'Display all Asciidoc attributes that are passed from Gradle'
        asciidocAttributes.group = 'Documentation'
        asciidocAttributes.doLast {
            println 'Current Asciidoctor Attributes'
            println '=============================='
            asciidoc.attributes.each { Object k, Object v ->
                println "${k.toString()}: ${v.toString()}"
            }
            println "gradle-version: ${minimumGradleVersion.get()}"
            println "user-manual: https://docs.gradle.org/${minimumGradleVersion.get()}/userguide/"
            println "language-reference: https://docs.gradle.org/${minimumGradleVersion.get()}/dsl/"
            println "api-reference: https://docs.gradle.org/${minimumGradleVersion.get()}/javadoc/"
        }

        asciidoc.inputs.property("minimumGradleVersion", minimumGradleVersion)
        asciidoc.doFirst {
            asciidoc.attributes.put('gradle-version', minimumGradleVersion.get())
            asciidoc.attributes.put('user-manual', "https://docs.gradle.org/${minimumGradleVersion.get()}/userguide/")
            asciidoc.attributes.put('language-reference', "https://docs.gradle.org/${minimumGradleVersion.get()}/dsl/")
            asciidoc.attributes.put('api-reference', "https://docs.gradle.org/${minimumGradleVersion.get()}/javadoc/")
        }

        asciidoc.inputs.property('repositoryPath', guides.repositoryPath)
        asciidoc.doFirst {
            asciidoc.attributes.put('repo-path', guides.repositoryPath.get())
            asciidoc.attributes.put('repository-path', guides.repositoryPath.get())
        }

        asciidoc.inputs.property('guideTitle', guides.title)
        asciidoc.doFirst {
            asciidoc.attributes.put('guide-title', guides.title.get())
        }

        asciidoc.with {
            dependsOn project.tasks.getByPath('gradleRunner')
            sourceDir 'contents'
            outputDir { project.buildDir }
            backends 'html5'
            inputs.files('samples').withPropertyName('samplesDir').withPathSensitivity(PathSensitivity.RELATIVE).optional()

            attributes 'source-highlighter': 'prettify',
                    imagesdir            : 'images',
                    stylesheet           : null,
                    linkcss              : true,
                    docinfodir           : '.',
                    docinfo1             : '',
                    nofooter             : true,
                    icons                : 'font',
                    sectanchors          : true,
                    sectlinks            : true,
                    linkattrs            : true,
                    encoding             : 'utf-8',
                    idprefix             : '',
                    toc                  : 'auto',
                    toclevels            : 1,
                    'toc-title'          : 'Contents',
                    guides               : 'https://guides.gradle.org',
                    'user-manual-name'   : 'User Manual',
                    'projdir'            : project.projectDir,
                    'codedir'            : project.file('src/main'),
                    'testdir'            : project.file('src/test'),
                    'samplescodedir'     : project.file('samples/code'),
                    'samplesoutputdir'   : project.file('samples/output'),
                    'samples-dir'        : project.file('samples')

        }

        lazyConfigureMoreAsciidoc(asciidoc)

        project.tasks.getByName("assemble").dependsOn asciidoc

        // TODO - rework the above to use lazy configuration

        def asciidocIndexFile = project.layout.file(project.tasks.named("asciidoctor").map { new File(it.outputDir, "html5/index.html") })

        project.tasks.register("viewGuide", ViewGuide) {
            group = "Documentation"
            description = "Generates the guide and open in the browser"
            indexFile = asciidocIndexFile
        }
    }

    @CompileDynamic
    private void lazyConfigureMoreAsciidoc(AsciidoctorTask asciidoc) {

        GuidesExtension guide = (GuidesExtension)(asciidoc.project.extensions.getByName(BasePlugin.GUIDE_EXTENSION_NAME))

        asciidoc.configure {
            sources {
                include 'index.adoc'
            }
        }
    }

    private void addGitHubPages(Project project) {
        project.apply plugin : 'org.ajoberstar.git-publish'

        GitPublishExtension githubPages = (GitPublishExtension)(project.extensions.getByName('gitPublish'))
        GuidesExtension guide = (GuidesExtension)(project.extensions.getByName(BasePlugin.GUIDE_EXTENSION_NAME))
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

    @CompileDynamic
    private void addCloudCI(Project project) {
        project.apply plugin : 'org.ysb33r.cloudci'

        project.cloudci {
            travisci {
                check.dependsOn CHECK_LINKS_TASK

                gitPublishPush {
                    enabled = System.getenv('TRAVIS_BRANCH') == 'master' && System.getenv('TRAVIS_PULL_REQUEST') == 'false' && System.getenv('TRAVIS_OS_NAME') == 'linux'
                }

                if(System.getenv('TRAVIS_OS_NAME') != 'linux') {
                    project.tasks.each { t ->
                        if( t.name.startsWith('gitPublish')) {
                            t.enabled = false
                        }
                    }
                }
            }
        }
    }
}
