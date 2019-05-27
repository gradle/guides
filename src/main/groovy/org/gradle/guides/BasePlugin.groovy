/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.guides

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
        addGithubPages(project)
        addCloudCI(project)
        addCheckLinks(project)
    }

    @Inject
    protected ProviderFactory getProviderFactory() {
        throw new UnsupportedOperationException()
    }

    private GuidesExtension addGuidesExtension(Project project) {
        GuidesExtension result = project.extensions.create(GUIDE_EXTENSION_NAME, GuidesExtension)
        result.minimumGradleVersion.convention(project.gradle.gradleVersion)
        return result
    }

    private void addGradleRunnerSteps(Project project) {
        project.apply plugin : 'org.ysb33r.gradlerunner'
    }

    private void addCheckLinks(Project project) {
        CheckLinks task = project.tasks.create(CHECK_LINKS_TASK,CheckLinks)

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
        }

        asciidoc.with {
            dependsOn project.tasks.getByPath('gradleRunner')
            sourceDir 'contents'
            outputDir { project.buildDir }
            backends 'html5'
            inputs.dir('samples').withPropertyName('samplesDir').withPathSensitivity(PathSensitivity.RELATIVE)

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
                    'gradle-version'     : new StringProvider(minimumGradleVersion),
                    'user-manual-name'   : 'User Manual',
                    'user-manual'        : new StringProvider(minimumGradleVersion.map { "https://docs.gradle.org/$it/userguide/" }),
                    'language-reference' : new StringProvider(minimumGradleVersion.map { "https://docs.gradle.org/$it/dsl/" }),
                    'api-reference'      : new StringProvider(minimumGradleVersion.map { "https://docs.gradle.org/$it/javadoc/" }),
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

    // Deferred the String evaluation for Asciidoc task until it support Provider API.
    // Using a simple interpolating closure expression in a GString doesn't work as it's not serializable.
    private class StringProvider implements Serializable {
        private Provider<String> value

        StringProvider(Provider<String> value) {
            this.value = value
        }

        @Override
        boolean equals(o) {
            return toString().equals(o)
        }

        @Override
        int hashCode() {
            return toString().hashCode()
        }

        @Override
        String toString() {
            return value.get()
        }

        private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
        {
            String value = aInputStream.readUTF()
            this.value = providerFactory.provider({ value })
        }

        private void writeObject(ObjectOutputStream aOutputStream) throws IOException
        {
            aOutputStream.writeUTF(toString())
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

        asciidoc.project.afterEvaluate {
            asciidoc.attributes 'repo-path' : guide.repoPath
        }
    }

    private void addGithubPages(Project project) {
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
                githubPages.repoUri.set("https://github.com/${guide.repoPath}.git".toString())
            } else {
                githubPages.repoUri.set("git@github.com:${guide.repoPath}.git".toString())
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
