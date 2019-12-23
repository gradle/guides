package org.gradle.samples.internal.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.Sync;

import javax.inject.Inject;

// TODO: Upstream this
public abstract class SyncWithProvider extends Sync {
    @Inject
    public SyncWithProvider(ProjectLayout layout, ProviderFactory providers) {
        getDestinationDirectory().set(layout.dir(providers.provider(() -> getDestinationDir())));
    }
    @OutputDirectory
    public abstract DirectoryProperty getDestinationDirectory();
}
