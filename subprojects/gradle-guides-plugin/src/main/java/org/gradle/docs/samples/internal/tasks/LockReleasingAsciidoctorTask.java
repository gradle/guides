package org.gradle.docs.samples.internal.tasks;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.internal.work.WorkerLeaseService;

import javax.inject.Inject;

public abstract class LockReleasingAsciidoctorTask extends AsciidoctorTask {
    @Inject
    public abstract WorkerLeaseService getWorkerLeaseService();

    @Override
    public void processAsciidocSources() {
        getWorkerLeaseService().withoutProjectLock(super::processAsciidocSources);
    }
}
