//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2017
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//

package org.gradle.guides

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.ajoberstar.gradle.git.ghpages.GithubPagesPluginExtension
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

@CompileStatic
class BasePlugin implements Plugin<Project> {

    static final String GUIDE_EXTENSION_NAME = 'guide'
    static final String CHECK_LINKS_TASK = 'checkLinks'

    void apply(Project project) {
        project.apply plugin : org.gradle.api.plugins.BasePlugin

        addGuidesExtension(project)
        addAsciidoctor(project)
        addGithubPages(project)
        addCloudCI(project)
        addCheckLinks(project)
        addGradleRunnerSteps(project)
    }

    private void addGuidesExtension(Project project) {
        project.extensions.create(GUIDE_EXTENSION_NAME,GuidesExtension,project)
    }

    private void addCheckLinks(Project project) {
        CheckLinks task = project.tasks.create(CHECK_LINKS_TASK,CheckLinks)

        AsciidoctorTask asciidoc = (AsciidoctorTask)(project.tasks.getByName('asciidoctor'))
        task.indexDocument = {  project.file("${asciidoc.outputDir}/html5/index.html") }

    }

    private void addAsciidoctor(Project project) {

        String gradleVersion = project.gradle.gradleVersion

        project.apply plugin: 'org.asciidoctor.convert'

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
            sourceDir 'contents'
            outputDir { project.buildDir }
            backends 'html5'

            attributes "source-highlighter": "coderay",
                    "coderay-linenums-mode": "table",
                    imagesdir            : 'images',
                    stylesheet           : null,
                    linkcss              : true,
                    docinfodir           : '.',
                    docinfo1             : '',
                    icons                : 'font',
                    sectanchors          : true,
                    sectlinks            : true,
                    linkattrs            : true,
                    encoding             : 'utf-8',
                    idprefix             : '',
                    toc                  : 'right',
                    toclevels            : 1,
                    guides               : 'https://guides.gradle.org',
                    'gradle-version'     : gradleVersion,
                    'user-manual-name'   : 'User Manual',
                    'user-manual'        : "https://docs.gradle.org/${gradleVersion}/userguide/",
                    'language-reference' : "https://docs.gradle.org/${gradleVersion}/dsl/",
                    'api-reference'      : "https://docs.gradle.org/${gradleVersion}/javadoc/",
                    'projdir'            : project.projectDir.absolutePath,
                    'codedir'            : project.file('src/main').absolutePath,
                    'testdir'            : project.file('src/test').absolutePath

        }

        addAsciidocExtensions(asciidoc)
        lazyConfigureMoreAsciidoc(asciidoc)
    }

    @CompileDynamic
    private void addAsciidocExtensions(AsciidoctorTask asciidoc) {
        asciidoc.extensions {
            postprocessor { document, output ->
                if (document.basebackend("html")) {
                    def extra = """<div style="padding-top: 10px;"><a href="https://guides.gradle.org"><img src="http://guides.gradle.org/gradle-guides.svg" alt=""></a></div>"""
                    output.replaceAll( ~/<div id="header">\s*<h1>/, """<div id="header">${extra}<h1>""")
                } else {
                    output
                }
            }
        }
    }

    @CompileDynamic
    private void lazyConfigureMoreAsciidoc(AsciidoctorTask asciidoc) {

        GuidesExtension guide = (GuidesExtension)(asciidoc.project.extensions.getByName(org.gradle.guides.BasePlugin.GUIDE_EXTENSION_NAME))

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
        project.apply plugin : 'org.ajoberstar.github-pages'

        GithubPagesPluginExtension githubPages = (GithubPagesPluginExtension)(project.extensions.getByName('githubPages'))
        GuidesExtension guide = (GuidesExtension)(project.extensions.getByName(org.gradle.guides.BasePlugin.GUIDE_EXTENSION_NAME))
        AsciidoctorTask asciidoc = (AsciidoctorTask)(project.tasks.getByName('asciidoctor'))
        String ghToken = System.getenv("GH_TOKEN")

        githubPages.commitMessage = "Publish to GitHub Pages"
        githubPages.pages.from {"${asciidoc.outputDir}/${asciidoc.backends[0]}"}

        if (ghToken) {
            githubPages.repoUri = "https://github.com/${guide.repoPath}.git"
            githubPages.credentials.username = ghToken
            githubPages.credentials.password = "\n"
        } else {
            githubPages.repoUri = "git@github.com:${guide.repoPath}.git"
        }
    }

    @CompileDynamic
    void addCloudCI(Project project) {
        project.apply plugin : 'org.ysb33r.cloudci'

        project.travisci  {

            check.dependsOn CHECK_LINKS_TASK

            publishGhPages {
                enabled = System.getenv('TRAVIS_BRANCH') == 'master' && System.getenv('TRAVIS_PULL_REQUEST') == 'false'
            }
        }
    }

    void addGradleRunnerSteps(Project project) {
        project.apply plugin : 'org.ysb33r.gradlerunner'
    }
}
