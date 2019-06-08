package org.gradle.guides

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction

/**
 * Generate the .editorconfig configuration file.
 *
 * @since 0.15.8
 */
@CompileStatic
abstract class GenerateEditorConfiguration extends GeneratorTask {
    @TaskAction
    private void doGenerate() {
        getOutputFile().asFile.get().text = """${hashComment(generatedFileHeader)}

root = true

[*]
indent_style = space
indent_size = 4
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[{*.html,*.js,*.css}]
indent_size = 2

[gradlew.bat]
end_of_line = crlf
"""
    }
}
