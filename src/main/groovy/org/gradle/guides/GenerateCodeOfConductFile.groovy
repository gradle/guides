package org.gradle.guides

import org.gradle.api.tasks.TaskAction

/**
 * Generate the code of conduct file.
 *
 * @since 0.15.8
 */
abstract class GenerateCodeOfConductFile extends GeneratorTask {
    @TaskAction
    private void doGenerate() {
        getOutputFile().asFile.get().text = """${htmlComment(generatedFileHeader)}

# Code of Conduct

In order to foster a more inclusive community, Gradle has adopted the [Contributor Covenant](https://www.contributor-covenant.org/version/1/4/code-of-conduct/).

Contributors must follow the Code of Conduct outlined at [https://gradle.org/conduct/](https://gradle.org/conduct/).
"""
    }
}
