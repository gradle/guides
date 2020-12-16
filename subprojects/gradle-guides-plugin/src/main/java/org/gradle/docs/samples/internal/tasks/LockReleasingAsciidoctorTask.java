package org.gradle.docs.samples.internal.tasks;

import org.asciidoctor.gradle.jvm.AsciidoctorTask;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public abstract class LockReleasingAsciidoctorTask extends AsciidoctorTask {

    @Inject
    public LockReleasingAsciidoctorTask(WorkerExecutor we) {
        super(we);
    }

    @Inject
    public abstract WorkerLeaseService getWorkerLeaseService();

    @Override
    public void processAsciidocSources() {
        getWorkerLeaseService().withoutProjectLock(super::processAsciidocSources);
    }
}
