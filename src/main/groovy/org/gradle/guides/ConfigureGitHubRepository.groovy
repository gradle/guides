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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Configure GitHub repository settings.
 *
 * @since 0.15.8
 */
@CompileStatic
abstract class ConfigureGitHubRepository extends DefaultTask {
    @Internal
    abstract Property<String> getRepositorySlug()

    @Internal
    abstract Property<String> getRepositoryDescription()

    @Internal
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
        try {
            outStream.withPrintWriter {
                it.println(content)
            }
        } finally {
            outStream.close()
        }
        int result = connection.getResponseCode()
        if (result != HttpURLConnection.HTTP_OK) {
            throw new GradleException(String.format("The GitHub API call failed with %s (%d)", connection.getResponseMessage(), connection.getResponseCode()))
        }
    }

    @CompileDynamic
    private static String askForGitHubCredentials() {
        if (System.getenv().containsKey("GITHUB_TOKEN")) {
            return System.getenv("GITHUB_TOKEN")
        } else {
            UserInputHandler inputHandler = getServices().get(UserInputHandler.class)
            return inputHandler.askQuestion("GitHub OAuth token: ", getProjectName());
        }
    }
}
