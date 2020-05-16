package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.work.WorkerLeaseService;

import javax.inject.Inject;

/**
 * Installs the sample's zip to the given directory.
 */
public abstract class InstallSample extends DefaultTask {
    @InputFiles
    @SkipWhenEmpty
    protected FileTree getSourceAsTree() {
        return getSource().getAsFileTree();
    }

    @Internal
    public abstract ConfigurableFileCollection getSource();

    @OutputDirectory
    public abstract DirectoryProperty getInstallDirectory();

    @Inject
    public abstract WorkerLeaseService getWorkerLeaseService();

    @TaskAction
    private void doInstall() {
        // TODO: Use the Worker API instead of releasing lock manually
        getWorkerLeaseService().withoutProjectLock(() -> {
            getProject().sync(spec -> {
                spec.from(getSource());
                spec.into(getInstallDirectory());
            });
        });
    }
}
