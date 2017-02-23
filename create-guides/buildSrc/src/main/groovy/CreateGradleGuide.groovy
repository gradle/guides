import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.options.Option
import org.apache.tools.ant.filters.ReplaceTokens
import org.kohsuke.github.GitHub

@CompileStatic
class CreateGradleGuide extends DefaultTask {

    enum GuideType {

        GETTING_STARTED('gs-template',),
        TOPICAL('topical-template')

        private GuideType(final String name) {
            templateName = name
            template = "https://github.com/gradle-guides/${name}".toURI()
        }

        final String templateName
        final URI template

        static GuideType byName(final String name) {
            String searchTerm = name.toUpperCase().replaceAll('-', '_')
            GuideType.values().find {
                it.name() == searchTerm
            }
        }
    }

    CreateGradleGuide() {
        super()
        enabled = !project.gradle.startParameter.isOffline() && gitHubRepoDoesNotExist()
        group = 'Gradle guide management'
    }


    @Input
    String templateBranch = 'master'

    @Option(option = "guide-type", description = "getting-started|topical|tutorial")
    String guideType

    @Input
    GuideType getGuideDetails() {
        this.guideType ? GuideType.byName(this.guideType) : null
    }

    @Option(option = "guide-issue", description = "Github issue in the form org/repo#issue")
    @Input
    String issue

    URI getGitHubURI() {
        "https://github.com/gradle-guides/${guideName}".toURI()
    }


    URI getArchiveURI() {
        "${guideDetails.template}/archive/${getTemplateBranch()}.zip".toURI()
    }

    File repoBaseDir = new File(project.buildDir, 'new-guide-repositories')
    String guideOrgUrl = 'https://github.com/gradle-guides'

    @Option(option = "guide-name", description = "Name of guide to be created")
    @Input
    String guideName

    String getGuideSlug() {
        guideName?.toLowerCase()?.replaceAll(~/\s+/, '-')
    }

    File getRepoDir() {
        new File(repoBaseDir, guideSlug)
    }

    String getGitHubAuthToken() {
        this.authToken ?: (String) (project.properties['gradle.guides.authToken'])
    }

    void setAuthToken(final String authToken) {
        this.authToken = authToken
    }

    @TaskAction
    void exec() {
        // Check that we have an auth token set before executing
        if (gitHubAuthToken == null) {
            throw new GradleException("Project property 'gradle.guides.authToken' not set. Otherwise set the authToken property on task '${this.name}'.")
        }

        // Check that we have a valid issue number
        if (!(issue ==~ /.+?\/.+?#\d+/)) {
            throw new GradleException('Github issue must be in the form org/repo#issue')
        }

        File zipFile = downloadZipFile(getTemporaryDir())

        File destDir = getRepoDir()
        destDir.mkdirs()
        createRepoFromZip(zipFile, destDir)

        final String guideRepo = "${guideOrgUrl}/${guideSlug}"
        createRepoOnGitHub(destDir,guideRepo.toURI())
        pushRemote(destDir,guideRepo)

        travis 'login', '-g', getGitHubAuthToken()

        try {
            travis 'enable'
            travis 'encrypt', '--add', '--override', "GH_TOKEN=${getGitHubAuthToken()}"
        } finally {
            travis 'logout', '--org'
        }

        commitTravisConfig(destDir)

    }

    @CompileDynamic
    private File downloadZipFile(final File destDir) {
        String zipPath = "zip:${archiveURI.toString()}!"
        project.vfs {
            cp zipPath, destDir, overwrite: true, recursive: true
        }
        new File(destDir, "${guideDetails.templateName}-${getTemplateBranch()}")
    }

    @CompileDynamic
    private Grgit createRepoFromZip(final File zipUnpacked, final File destDir) {
        project.copy {
            from zipUnpacked, {
                include '**'
                filter ReplaceTokens, beginToken: '@@', endToken: '@@', tokens: [
                    'GUIDE_NAME': getGuideName(),
                    'GUIDE_SLUG': getGuideSlug()
                ]
            }
            filesMatching 'gradlew*', {
              mode = 0755
            }
            includeEmptyDirs true
            into destDir
        }
        Grgit grgit = Grgit.init(dir: destDir)
        grgit.add(patterns: ['.'])
        grgit.commit(message: "Initialize ${guideSlug} repository ($issue)", all: true)
        return grgit
    }

    @CompileDynamic
    private void pushRemote(final File repoDir,final String url) {
        Grgit grgit = Grgit.open(
            dir : repoDir,
            creds : new Credentials( username :getGitHubAuthToken(), password: '' )
        )
        grgit.remote.add name: 'origin', url: url, pushUrl: url
        grgit.push()
    }

    @CompileDynamic
    void travis( final String cmd, String... args) {
        project.jrubyexec {
            jrubyArgs '-S'
            script 'travis'
            scriptArgs cmd
            scriptArgs '--org', '--no-interactive'
            scriptArgs args
            workingDir getRepoDir()
        }
    }

    private void createRepoOnGitHub(final File repoDir, final URI uri) {
        GitHub gh = GitHub.connectUsingOAuth(getGitHubAuthToken())
        gh.getOrganization('gradle-guides').
            createRepository(getGuideSlug()).
            description(getGuideName()).
            issues(true).
            wiki(false).
            private_(false).
            create()
    }

    @CompileDynamic
    private void commitTravisConfig(final File repoDir) {
        Grgit grgit = Grgit.open(
            dir : repoDir,
            creds : new Credentials( username :getGitHubAuthToken(), password: '' )
        )
        grgit.commit(message: "Set GH_TOKEN", all: true)
        grgit.push()
    }

    @CompileDynamic
    private boolean gitHubRepoDoesNotExist() {
        project.vfs { !exists(getGitHubURI().toString()) }
    }

    private String authToken
}
