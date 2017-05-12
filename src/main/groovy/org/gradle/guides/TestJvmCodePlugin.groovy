package org.gradle.guides

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @since 0.1
 */
@CompileStatic
class TestJvmCodePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin : 'groovy'
        project.apply plugin : BasePlugin

        addDependencies(project)
    }

    @CompileDynamic
    void addDependencies(Project project) {
        def spockGroovyVer = GroovySystem.version.replaceAll(/\.\d+$/,'')

        project.dependencies {
            testCompile localGroovy()
            testCompile ("org.spockframework:spock-core:1.0-groovy-${spockGroovyVer}") {
                exclude module : 'groovy-all'
            }
            testCompile 'commons-io:commons-io:2.5'
            testCompile gradleTestKit()
        }
    }
}
