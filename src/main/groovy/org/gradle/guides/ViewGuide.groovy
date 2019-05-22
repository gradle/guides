package org.gradle.guides

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import java.awt.*

abstract class ViewGuide extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getIndexFile()

    ViewGuide() {
        outputs.upToDateWhen(Specs.satisfyNone())
    }

    @TaskAction
    void action() {
        Desktop.desktop.open(getIndexFile().get().asFile)
    }
}
