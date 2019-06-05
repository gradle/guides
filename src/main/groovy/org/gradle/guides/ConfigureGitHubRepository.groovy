package org.gradle.guides

import groovy.swing.SwingBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction

/**
 * Configure GitHub repository settings.
 *
 * @since 0.15.8
 */
@CompileStatic
abstract class ConfigureGitHubRepository extends DefaultTask {
    abstract Property<String> getRepositorySlug()
    abstract Property<String> getRepositoryDescription()
    abstract Property<URL> getRepositoryHomepage()

    @TaskAction
    private void doConfigure() {
        if (!repositorySlug.isPresent()) {
            setDidWork(false)
            return
        }

        def credentials = askForGitHubCredentials()

        editRepository(credentials, repositorySlug.get(), repositoryDescription.get(), repositoryHomepage.get().toString())
    }

    private static void editRepository(String oauth, String slug, String description, String homepage) {
        sendGitHubApiCall(new URL("https://api.github.com/repos/$slug"), oauth, "application/vnd.github.v3+json", "PATCH", """{
    "name": "${slug.split('/')[1]}",
    "description": "${description}",
    "homepage": "${homepage}",
    "has_issues": true,
    "has_projects": true,
    "has_wiki": false,
    "allow_squash_merge": true,
    "allow_merge_commit": true,
    "allow_rebase_merge": true
}""")
    }

    private static void sendGitHubApiCall(URL apiUrl, String oauth, String accept, String method, String content) {
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection()
        connection.setRequestProperty("Authorization", "token $oauth")
        connection.setRequestProperty("Accept", accept)
        connection.setRequestProperty("Method", method)
        connection.setDoOutput(true)
        def outStream = connection.getOutputStream()
        outStream.withPrintWriter {
            it.println(content)
        }
        outStream.close()
        int result = connection.getResponseCode()
        if (result != HttpURLConnection.HTTP_OK) {
            throw new GradleException(String.format("The GitHub API call failed with %s (%d)", connection.getResponseMessage(), connection.getResponseCode()))
        }
    }

    @CompileDynamic
    private static String askForGitHubCredentials() {
        if (System.getenv().containsKey("GITHUB_TOKEN")) {
            return System.getenv("GITHUB_TOKEN")
        } else if(System.console() == null) {
            def result = null
            new SwingBuilder().edt {
                dialog(modal: true, // Otherwise the build will continue running before you closed the dialog
                        title: 'GitHub Credentials', // Dialog title
                        alwaysOnTop: true, // pretty much what the name says
                        resizable: false, // Don't allow the user to resize the dialog
                        locationRelativeTo: null, // Place dialog in center of the screen
                        pack: true, // We need to pack the dialog (so it will take the size of it's children)
                        show: true // Let's show it
                ) {
                    vbox() {
                        label(text: "OAuth:")
                        def oauthInput = passwordField()
                        button(defaultButton: true, text: 'OK', actionPerformed: {
                            result = new String(oauthInput.password)
                            dispose()
                        })
                    }
                }
            }
            return result
        } else {
            return new String(System.console().readPassword("\nOAuth: "))
        }
    }
}
