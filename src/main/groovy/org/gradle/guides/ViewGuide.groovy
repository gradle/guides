package org.gradle.guides

import org.gradle.api.DefaultTask
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class ViewGuide extends DefaultTask {

    @Internal
    File outputDir

    ViewGuide() {
        outputs.upToDateWhen(Specs.satisfyNone())
    }

    @TaskAction
    void action() {
        if (isMacOs()) {
            def url = getOutputDir().absoluteFile.toURI().toASCIIString()
                    .replaceFirst("^file:/(?!=/)", "file:///")

            project.exec {
                it.commandLine "osascript", "-e", """
                    try
                        tell application "Finder" to get application file id "com.google.Chrome"
                        set appExists to true
                    on error
                        set appExists to false
                    end try
                    if appExists then
                        tell application "Google Chrome"
                            set theUrl to "$url"
                            set found to false
                            set theWindowIndex to 0
                            repeat with theWindow in every window
                                set theTabIndex to 0
                                repeat with theTab in every tab of theWindow
                                    set theTabIndex to theTabIndex + 1
                                    if theTab's URL starts with theUrl then
                                        set found to true
                                        tell application "Google Chrome" to set active tab index of theWindow to theTabIndex
                                        tell theTab to reload
                                    end if
                                end repeat
                            end repeat
                            if not found then
                                set URL of (active tab of (make new window)) to theUrl & "/html5/index.html"
                            end if
                        end tell
                    end if
                """
            }
        } else {
            didWork = false
        }
    }

    private static boolean isMacOs() {
        System.getProperty("os.name").toLowerCase().contains("mac")
    }
}
