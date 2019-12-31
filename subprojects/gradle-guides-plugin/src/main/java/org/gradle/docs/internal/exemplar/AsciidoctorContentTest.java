package org.gradle.docs.internal.exemplar;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;

public abstract class AsciidoctorContentTest extends DefaultTask {
    @InputFiles
    public abstract ConfigurableFileCollection getContentFiles();

    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @Internal
    public abstract DirectoryProperty getGradleUserHomeDirectoryForTesting();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    private void doTest() throws IOException {
        WorkQueue workQueue = getWorkerExecutor().classLoaderIsolation(spec -> {
            spec.getClasspath().from(getClasspath());
        });

        workQueue.submit(AsciidoctorContentTestWorkerAction.class, parameter -> {
            parameter.getContentFiles().from(getContentFiles());
            parameter.getWorkspaceDirectory().set(getTemporaryDir());
            parameter.getGradleUserHomeDirectory().set(getGradleUserHomeDirectoryForTesting());
        });
    }
}
