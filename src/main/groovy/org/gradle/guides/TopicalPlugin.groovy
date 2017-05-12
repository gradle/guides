package org.gradle.guides

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class TopicalPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply plugin : BasePlugin
    }
}
