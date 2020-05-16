package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.Sync;
import org.gradle.internal.work.WorkerLeaseService;

import javax.inject.Inject;

// TODO: Upstream this
public abstract class SyncWithProvider extends Sync {
    @Inject
    public SyncWithProvider(ProjectLayout layout, ProviderFactory providers) {
        getDestinationDirectory().set(layout.dir(providers.provider(() -> getDestinationDir())));
    }

    @Inject
    public abstract WorkerLeaseService getWorkerLeaseService();

    @Override
    protected void copy() {
        getWorkerLeaseService().withoutProjectLock(() -> super.copy());
    }

    @OutputDirectory
    public abstract DirectoryProperty getDestinationDirectory();
}
