import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleGuidePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.with {
            apply plugin: 'base'
            apply plugin: 'org.ysb33r.vfs'
            apply plugin: 'com.github.jruby-gradle.base'

            dependencies {
                jrubyExec 'rubygems:travis:1.8.8'
            }

            tasks.create( 'create', CreateGradleGuide ) {
                description = 'Create new guide'
                dependsOn 'jrubyPrepare'
            }

        }
    }
}
