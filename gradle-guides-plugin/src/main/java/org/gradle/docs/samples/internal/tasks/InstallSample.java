package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.work.WorkerLeaseService;

import javax.inject.Inject;
import java.util.Collections;

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

    @Input
    @Optional
    public abstract Property<String> getReadmeName();

    @Input
    @Optional
    public abstract ListProperty<String> getExcludes();

    @OutputDirectory
    public abstract DirectoryProperty getInstallDirectory();

    @Inject
    public abstract WorkerLeaseService getWorkerLeaseService();

    @Inject
    protected abstract FileSystemOperations getFs();

    @TaskAction
    public void doInstall() {
        // TODO: Use the Worker API instead of releasing lock manually
        getWorkerLeaseService().runAsIsolatedTask(() -> {
            getFs().sync(spec -> {
                spec.from(getSource());
                spec.into(getInstallDirectory());
                spec.exclude(getExcludes().getOrElse(Collections.emptyList()));
                spec.rename(getReadmeName().getOrElse("README"), "README");
            });
        });
    }
}
