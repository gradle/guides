package org.gradle.guides

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

//project.tasks.register("initGuide") {
//    it.group = GUIDE_SETUP_GROUP_NAME
//    it.description = "Initialize Guide repository"
//    it.dependsOn(setupGuideTask, "wrapper")
//    it.onlyIf {
//        return !project.file("contents/index.adoc").exists()
//    }
//    it.doLast {
//        project.copy {
//            from "https://.../gs/index.adoc"
//            filter ReplaceTokens, beginToken: '@@', endToken: '@@', tokens: [
//                    'GUIDE_NAME': "Some title", //getGuideName(),
//                    'GUIDE_SLUG': "gradle-guides/slug" //getGuideSlug()
//            ]
//            into project.projectDir
//        }
//    }
//}
abstract class InitGuide extends DefaultTask {
    @Input
    abstract Property<String> getType()

    @Optional
    @Input
    @Option(option = "title", description = "Set the title of the guide to generate.")
    abstract Property<String> getTitle()


    @TaskAction
    private void doInit() {

    }
}
