package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public abstract class InstallSampleTask extends DefaultTask {
    @SkipWhenEmpty
    @InputFiles
    protected FileTree getInputFiles() {
        return getSource().getAsFileTree();
    }

    @Internal
    public abstract ConfigurableFileCollection getSource();

    @OutputDirectory
    public abstract DirectoryProperty getInstallDirectory();

    @TaskAction
    private void doInstall() {
        getProject().sync(spec -> {
            spec.from(getSource());
            spec.into(getInstallDirectory());
        });
    }
}
