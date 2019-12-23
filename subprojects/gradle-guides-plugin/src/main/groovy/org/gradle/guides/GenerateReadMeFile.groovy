package org.gradle.guides

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Generate Asciidoc README for the current repository
 */
@CompileStatic
abstract class GenerateReadMeFile extends GeneratorTask {
    @Input
    abstract Property<String> getRepositorySlug()

    @Input
    abstract Property<String> getTitle()

    @TaskAction
    private void doGenerate() {
        if (!getOutputFile().asFile.get().name.endsWith(".adoc")) {
            throw new GradleException("Only Asciidoc README file is supported")
        }

        getOutputFile().asFile.get().text = """${slashComment(generatedFileHeader)}

= ${getTitle().get()} image:https://travis-ci.org/${repositorySlug.get()}.svg?branch=master["Build Status", link="https://travis-ci.org/${repositorySlug.get()}?branch=master"]

* Content for this guide is in Asciidoc format and the entry file is `contents/index.adoc`.
* Place new images under `contents/images`.
* Add testable code snippets to `src` folder as appropriate for this guide.
* Commits to `master` will automatically be published to `gh-pages` if the build is successful.
* To build documentation run `./gradlew asciidoctor` and find the results at `build/html5/index.html`.
* To test everything run `./gradlew build`.
* CI builds are at https://travis-ci.org/${repositorySlug.get()}
"""
    }
}
